// Copyright 2014 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'base/common.dart';
import 'base/file_system.dart';
import 'cache.dart';
import 'globals.dart' as globals;

/// Expands templates in a directory to a destination. All files that must
/// undergo template expansion should end with the '.tmpl' extension. All other
/// files are ignored. In case the contents of entire directories must be copied
/// as is, the directory itself can end with '.tmpl' extension. Files within
/// such a directory may also contain the '.tmpl' extension and will be
/// considered for expansion. In case certain files need to be copied but
/// without template expansion (images, data files, etc.), the '.copy.tmpl'
/// extension may be used.
///
/// Folders with platform/language-specific content must be named
/// '<platform>-<language>.tmpl'.
///
/// Files in the destination will contain none of the '.tmpl', '.copy.tmpl'
/// or '-<language>.tmpl' extensions.
class Template {
  Template(Directory templateSource, Directory baseDir) {
    _templateFilePaths = <String, String>{};

    if (!templateSource.existsSync()) {
      return;
    }

    final List<FileSystemEntity> templateFiles = templateSource.listSync(recursive: true);

    for (final FileSystemEntity entity in templateFiles) {
      if (entity is! File) {
        // We are only interesting in template *file* URIs.
        continue;
      }

      final String relativePath = globals.fs.path.relative(entity.path,
          from: baseDir.absolute.path);

      if (relativePath.contains(templateExtension)) {
        // If '.tmpl' appears anywhere within the path of this entity, it is
        // is a candidate for rendering. This catches cases where the folder
        // itself is a template.
        _templateFilePaths[relativePath] = globals.fs.path.absolute(entity.path);
      }
    }
  }

  factory Template.fromName(String name) {
    // All named templates are placed in the 'templates' directory
    final Directory templateDir = templateDirectoryInPackage(name);
    return Template(templateDir, templateDir);
  }

  static const String templateExtension = '.tmpl';
  static const String copyTemplateExtension = '.copy.tmpl';
  final Pattern _kTemplateLanguageVariant = RegExp(r'(\w+)-(\w+)\.tmpl.*');

  Map<String /* relative */, String /* absolute source */> _templateFilePaths;

  /// Render the template into [directory].
  ///
  /// May throw a [ToolExit] if the directory is not writable.
  int render(
    Directory destination,
    Map<String, dynamic> context, {
    bool overwriteExisting = true,
    bool printStatusWhenWriting = true,
  }) {
    try {
      destination.createSync(recursive: true);
    } on FileSystemException catch (err) {
      globals.printError(err.toString());
      throwToolExit('Failed to flutter create at ${destination.path}.');
      return 0;
    }
    int fileCount = 0;

    /// Returns the resolved destination path corresponding to the specified
    /// raw destination path, after performing language filtering and template
    /// expansion on the path itself.
    ///
    /// Returns null if the given raw destination path has been filtered.
    String renderPath(String relativeDestinationPath) {
      final Match match = _kTemplateLanguageVariant.matchAsPrefix(relativeDestinationPath);
      if (match != null) {
        final String platform = match.group(1);
        final String language = context['${platform}Language'] as String;
        if (language != match.group(2)) {
          return null;
        }
        relativeDestinationPath = relativeDestinationPath.replaceAll('$platform-$language.tmpl', platform);
      }
      // Only build a web project if explicitly asked.
      final bool web = context['web'] as bool;
      if (relativeDestinationPath.contains('web') && !web) {
        return null;
      }
      // Only build a Linux project if explicitly asked.
      final bool linux = context['linux'] as bool;
      if (relativeDestinationPath.startsWith('linux.tmpl') && !linux) {
        return null;
      }
      // Only build a macOS project if explicitly asked.
      final bool macOS = context['macos'] as bool;
      if (relativeDestinationPath.startsWith('macos.tmpl') && !macOS) {
        return null;
      }
      final String projectName = context['projectName'] as String;
      final String androidIdentifier = context['androidIdentifier'] as String;
      final String pluginClass = context['pluginClass'] as String;
      final String destinationDirPath = destination.absolute.path;
      final String pathSeparator = globals.fs.path.separator;
      String finalDestinationPath = globals.fs.path
        .join(destinationDirPath, relativeDestinationPath)
        .replaceAll(copyTemplateExtension, '')
        .replaceAll(templateExtension, '');

      if (androidIdentifier != null) {
        finalDestinationPath = finalDestinationPath
            .replaceAll('androidIdentifier', androidIdentifier.replaceAll('.', pathSeparator));
      }
      if (projectName != null) {
        finalDestinationPath = finalDestinationPath.replaceAll('projectName', projectName);
      }
      if (pluginClass != null) {
        finalDestinationPath = finalDestinationPath.replaceAll('pluginClass', pluginClass);
      }
      return finalDestinationPath;
    }

    _templateFilePaths.forEach((String relativeDestinationPath, String absoluteSourcePath) {
      final bool withRootModule = context['withRootModule'] as bool ?? false;
      if (!withRootModule && absoluteSourcePath.contains('flutter_root')) {
        return;
      }

      final String finalDestinationPath = renderPath(relativeDestinationPath);
      if (finalDestinationPath == null) {
        return;
      }
      final File finalDestinationFile = globals.fs.file(finalDestinationPath);
      final String relativePathForLogging = globals.fs.path.relative(finalDestinationFile.path);

      // Step 1: Check if the file needs to be overwritten.

      if (finalDestinationFile.existsSync()) {
        if (overwriteExisting) {
          finalDestinationFile.deleteSync(recursive: true);
          if (printStatusWhenWriting) {
            globals.printStatus('  $relativePathForLogging (overwritten)');
          }
        } else {
          // The file exists but we cannot overwrite it, move on.
          if (printStatusWhenWriting) {
            globals.printTrace('  $relativePathForLogging (existing - skipped)');
          }
          return;
        }
      } else {
        if (printStatusWhenWriting) {
          globals.printStatus('  $relativePathForLogging (created)');
        }
      }

      fileCount++;

      finalDestinationFile.createSync(recursive: true);
      final File sourceFile = globals.fs.file(absoluteSourcePath);

      // Step 2: If the absolute paths ends with a '.copy.tmpl', this file does
      //         not need mustache rendering but needs to be directly copied.

      if (sourceFile.path.endsWith(copyTemplateExtension)) {
        sourceFile.copySync(finalDestinationFile.path);

        return;
      }

      // Step 3: If the absolute path ends with a '.tmpl', this file needs
      //         rendering via mustache.

      if (sourceFile.path.endsWith(templateExtension)) {
        final String templateContents = sourceFile.readAsStringSync();
        final String renderedContents = globals.templateRenderer.renderString(templateContents, context);

        finalDestinationFile.writeAsStringSync(renderedContents);

        return;
      }

      // Step 4: This file does not end in .tmpl but is in a directory that
      //         does. Directly copy the file to the destination.

      sourceFile.copySync(finalDestinationFile.path);
    });

    return fileCount;
  }
}

Directory templateDirectoryInPackage(String name) {
  final String templatesDir = globals.fs.path.join(Cache.flutterRoot,
      'packages', 'flutter_tools', 'templates');
  return globals.fs.directory(globals.fs.path.join(templatesDir, name));
}
