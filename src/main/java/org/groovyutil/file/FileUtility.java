package org.groovyutil.file;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class FileUtility {
    private static final Logger logger = LoggerFactory.getLogger(FileUtility.class);

    public static String[] Dir(String pathName) throws IOException{
        return Dir(pathName, false);
    }

    public static String[] Dir(String pathName, boolean sort) throws IOException{

        File directory = new File(GetFilePath(pathName));
        FileFilter filter = new WildcardFileFilter(GetFileName(pathName));
        File[] files = directory.listFiles(filter);

        if (sort) {
            Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
        }

        String[] fileNames = new String[files.length];

        for (int fileCount = 0; fileCount < files.length; fileCount++) {
            fileNames[fileCount] = files[fileCount].getAbsolutePath();
        }

        return fileNames;
    }

    public static String OnlyOneFileInListing(String[] fileList, String errorMessage) throws Exception {
        if (fileList.length != 1) {
            throw new Exception("Only One File For " + errorMessage + "There are " + fileList.length + " files");
        }
        return fileList[0];
    }

    public static String GetFilePath(String fullFilename) throws IOException {
        String path = FilenameUtils.getFullPath(fullFilename);
        logger.debug("Full filename = {}, path = {}", fullFilename, path);
        return path;
    }

    public static String GetFileName(String fullFilename) throws IOException {
        String filename = FilenameUtils.getName(fullFilename);
        logger.debug("Full filename = {}, filename = {}", fullFilename, filename);
        return filename;
    }

    public static String[] CopyFile(String[] sourceFileNames, String destFilename) throws IOException {

        String[] fileList = new String[0];

        for (String sourceFilename : sourceFileNames) {
            String[] subFileList = CopyFile(sourceFilename, destFilename);
            fileList = ArrayUtils.addAll(fileList, subFileList);
        }
        return fileList;
    }

    public static String[] CopyFile(String sourceFilename, String destFilename) throws IOException{

        File destinationFile = new File(destFilename);
        File sourceFile = new File(sourceFilename);

        if (fileNameContainsWildcard(sourceFilename)) {
            if (!destinationFile.isDirectory()) {
                logger.error("CopyFile with wildcard source {} Destination {} is not a directory", sourceFilename, destFilename);
                throw new IOException("Copy Wildcard Source Destination is not a directory");
            }
            else {  //wildcard source and destination is directory
                logger.info("CopyFile Wildcard Source {} copied to directory {}", sourceFilename, destFilename);

                WildcardFileFilter wildcardFileFilter = new WildcardFileFilter(GetFileName(sourceFilename));
                Collection<File> sourceFiles = FileUtils.listFiles(new File(GetFilePath(sourceFilename)), wildcardFileFilter, null);
                return CopyFilesToDirectory(sourceFiles, destinationFile);
            }
        }
        else if (sourceFile.isDirectory()){  //not wildcard source
            if (!destinationFile.isDirectory()) {
                logger.error("CopyFile with directory source {} Destination {} is not a directory", sourceFilename, destFilename);
                throw new IOException("CopyFile Directory Source Destination is not a directory");
            }
            else {
                logger.info("CopyFile Directory Source {} copied to directory {}", sourceFilename, destFilename);
                Collection<File> sourceFiles = FileUtils.listFiles(new File(GetFilePath(sourceFilename)), null, false);
                return CopyFilesToDirectory(sourceFiles, destinationFile);
            }
        }
        else {
            String[] copiedFile = new String[1];

            if (destinationFile.isDirectory()) {
                FileUtils.copyFileToDirectory(sourceFile, destinationFile);
                logger.info("File {} Copied to Directory {}", sourceFilename, destFilename);
                copiedFile[0] = destFilename + GetFileName(sourceFilename);
            }
            else {
                logger.info("File {} Copied to File {}", sourceFilename, destFilename);
                FileUtils.copyFile(sourceFile, destinationFile);
                copiedFile[0] = destFilename;
            }
            return copiedFile;
        }
    }

    private static boolean fileNameContainsWildcard(String filename) {
        return (filename.contains("*") || filename.contains("?"));
    }

    private static String[] CopyFilesToDirectory(Collection<File> sourceFiles, File destinationFile) throws IOException {

        String[] fileList = new String[sourceFiles.size()];
        int fileCount = 0;
        for (File sf : sourceFiles) {
            FileUtils.copyFileToDirectory(sf, destinationFile);
            fileList[fileCount++] = sf.getAbsolutePath();
            logger.info("File {} Copied to {}", sf.getAbsolutePath(), destinationFile.getAbsolutePath());
        }

        return fileList;
    }

    public static String[] DeleteFile(String[] sourceFileNames) throws IOException {

        String[] fileList = new String[0];

        for (String sourceFilename : sourceFileNames) {
            String[] subFileList = DeleteFile(sourceFilename);
            fileList = ArrayUtils.addAll(fileList, subFileList);
        }
        return fileList;
    }

    public static String[] DeleteFile(String sourceFilename) throws IOException{

        File sourceFile = new File(sourceFilename);

        if (fileNameContainsWildcard(sourceFilename)) {
            //wildcard source
            logger.info("Wildcard Source {} Deleted", sourceFilename);

            WildcardFileFilter wildcardFileFilter = new WildcardFileFilter(GetFileName(sourceFilename));
            Collection<File> sourceFiles = FileUtils.listFiles(new File(GetFilePath(sourceFilename)), wildcardFileFilter, null);

            String[] fileList = new String[sourceFiles.size()];
            int fileCount = 0;
            for (File sf : sourceFiles) {
                FileUtils.forceDelete(sf);
                fileList[fileCount++] = sf.getAbsolutePath();
                logger.info("File {} Deleted", sf.getAbsolutePath());
            }

            return fileList;
        }
        else if (sourceFile.isDirectory()){  //not wildcard source

            logger.error("DeleteFile not allowed with directory source {}", sourceFilename);
            throw new IOException("DeleteFile not allowed on Directory Source");
        }
        else {
            String[] deletedFile = new String[1];

            logger.info("File {} Deleted", sourceFilename);
            FileUtils.forceDelete(sourceFile);
            deletedFile[0] = sourceFilename;

            return deletedFile;
        }
    }

    public static String[] MoveFile(String[] sourceFileNames, String destFilename) throws IOException {

        String[] fileList = new String[0];

        for (String sourceFilename : sourceFileNames) {
            String[] subFileList = MoveFile(sourceFilename, destFilename);
            fileList = ArrayUtils.addAll(fileList, subFileList);
        }
        return fileList;
    }

    public static String[] MoveFile(String sourceFilename, String destFilename) throws IOException{

        File destinationFile = new File(destFilename);
        File sourceFile = new File(sourceFilename);

        if (fileNameContainsWildcard(sourceFilename)) {
            if (!destinationFile.isDirectory()) {
                logger.error("MoveFile with wildcard source {} Destination {} is not a directory", sourceFilename, destFilename);
                throw new IOException("MoveFile Wildcard Source Destination is not a directory");
            }
            else {  //wildcard source and destination is directory
                logger.info("Wildcard Source {} moved to directory {}", sourceFilename, destFilename);

                WildcardFileFilter wildcardFileFilter = new WildcardFileFilter(GetFileName(sourceFilename));
                Collection<File> sourceFiles = FileUtils.listFiles(new File(GetFilePath(sourceFilename)), wildcardFileFilter, null);
                return MoveFilesToDirectory(sourceFiles, destinationFile);
            }
        }
        else if (sourceFile.isDirectory()){  //not wildcard source
            if (!destinationFile.isDirectory()) {
                logger.error("MoveFile with directory source {} Destination {} is not a directory", sourceFilename, destFilename);
                throw new IOException("MoveFile Directory Source Destination is not a directory");
            }
            else {
                logger.info("Directory Source {} moved to directory {}", sourceFilename, destFilename);
                Collection<File> sourceFiles = FileUtils.listFiles(new File(GetFilePath(sourceFilename)), null, false);
                return MoveFilesToDirectory(sourceFiles, destinationFile);
            }
        }
        else {
            String[] movedFile = new String[1];

            if (destinationFile.isDirectory()) {
                FileUtils.moveFileToDirectory(sourceFile, destinationFile, true);
                logger.info("File {} Moved to Directory {}", sourceFilename, destFilename);
                movedFile[0] = destFilename + GetFileName(sourceFilename);
            }
            else {
                logger.info("File {} Moved to File {}", sourceFilename, destFilename);
                FileUtils.moveFile(sourceFile, destinationFile);
                movedFile[0] = destFilename;
            }
            return movedFile;
        }
    }

    private static String[] MoveFilesToDirectory(Collection<File> sourceFiles, File destinationFile) throws IOException {

        String[] fileList = new String[sourceFiles.size()];
        int fileCount = 0;
        for (File sf : sourceFiles) {
            FileUtils.moveFileToDirectory(sf, destinationFile, true);
            fileList[fileCount++] = sf.getAbsolutePath();
            logger.info("File {} Moved to {}", sf.getAbsolutePath(), destinationFile.getAbsolutePath());
        }

        return fileList;
    }

    public static String[] ArchvieFile(String[] sourceFileNames) throws IOException {

        String[] fileList = new String[0];

        for (String sourceFilename : sourceFileNames) {
            String[] subFileList = ArchiveFile(sourceFilename);
            fileList = ArrayUtils.addAll(fileList, subFileList);
        }
        return fileList;
    }

    public static String[] ArchiveFile(String sourceFilename) throws IOException {

        String archiveSubFolder = GetDateTimeFormattedString("yyyy-MM/yyyy-MM-dd");
        String destRootPath = GetFilePath(sourceFilename);
        String destFilename = destRootPath + archiveSubFolder;
        logger.info("Archive File(s) {} to {}", sourceFilename, destFilename);

        MakeDirectory(destFilename);
        String[] archiveFiles = MoveFile(sourceFilename, destFilename);

        return archiveFiles;
    }

    public static String GetDateTimeFormattedString(String format) {
        LocalDateTime now = LocalDateTime.now();
        return now.format(DateTimeFormatter.ofPattern(format));
    }

    public static String MakeDirectory(String directoryName) throws IOException {
        File directory = new File(directoryName);

        if (directory.isDirectory()) {
            logger.debug("MakeDirectory directory {} already exists", directoryName);
        }
        else if (!directory.exists()){
            FileUtils.forceMkdir(directory);
            logger.info("MakeDirectory directory {} created", directoryName);
        }
        else {
            logger.error("MakeDirectory directory {} already exisits is a file", directoryName);
            throw new IOException("MakeDirectory directory exists and is not a directory");
        }

        return directoryName;
    }

    public static String[] ArchiveAndTimeStampFile(String[] sourceFileNames) throws IOException{

        String[] fileList = new String[0];

        for (String sourceFilename : sourceFileNames) {
            String[] subFileList = ArchiveAndTimeStampFile(sourceFilename);
            fileList = ArrayUtils.addAll(fileList, subFileList);
        }
        return fileList;
    }

    public static String[] ArchiveAndTimeStampFile(String sourceFileName) throws IOException {
        String timeStampFileName = TimeStampFile(sourceFileName);
        String[] archivedFileName = ArchiveFile(timeStampFileName);
        logger.info("ArchiveAndTimeStampFile() Original - <{}> New <{}>", sourceFileName, archivedFileName);
        return archivedFileName;
    }

    public static String[] ArchiveAndDateStampFile(String[] sourceFileNames) throws IOException{
        String[] fileList = new String[0];

        for (String sourceFilename : sourceFileNames) {
            String[] subFileList = ArchiveAndDateStampFile(sourceFilename);
            fileList = ArrayUtils.addAll(fileList, subFileList);
        }
        return fileList;
    }

    public static String[] ArchiveAndDateStampFile(String sourceFileName) throws IOException {
        String timeStampFileName = DateStampFile(sourceFileName);
        String[] archivedFileName = ArchiveFile(timeStampFileName);
        logger.info("ArchiveAndDateStampFile() Original - <{}> New <{}>", sourceFileName, archivedFileName);
        return archivedFileName;
    }

    public static void DeleteDirectory(String directoryName) throws IOException {
        CleanDirectory(directoryName);
        File directory = new File(directoryName);
        FileUtils.deleteDirectory(directory);
        directory.delete();
        logger.debug("DeleteDirectory {} deleted", directoryName);
    }

    public static void CleanDirectory(String directoryName) throws IOException {
        File directory = new File(directoryName);
        FileUtils.cleanDirectory(directory);
    }

    public static String DateStampFile(String sourceFilename) throws IOException {
        String dateStampedFileName = GetFilePath(sourceFilename) +
                GetFileBaseName(sourceFilename) + "." +
                GetDateString() + "." +
                GetFileExtension(sourceFilename);
        logger.info("Source File {} Time Stamped to {}", sourceFilename, dateStampedFileName);
        RenameFile(sourceFilename, dateStampedFileName);
        return dateStampedFileName;
    }

    public static String GetDateString() {
        LocalDate now = LocalDate.now();
        return now.format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    public static String TimeStampFile(String sourceFilename) throws IOException {
        String timeStampedFileName = GetFilePath(sourceFilename) +
                GetFileBaseName(sourceFilename) + "." +
                GetDateTimeString() + "." +
                GetFileExtension(sourceFilename);
        logger.info("Source File {} Time Stamped to {}", sourceFilename, timeStampedFileName);
        RenameFile(sourceFilename, timeStampedFileName);
        return timeStampedFileName;
    }

    public static String GetFileBaseName(String fullFilename) throws IOException {
        String baseName = FilenameUtils.getBaseName(fullFilename);
        logger.debug("Full filename = {}, base name = {}", fullFilename, baseName);
        return baseName;
    }

    public static String GetDateTimeString() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
    }

    public static String GetFileExtension(String fullFilename) throws IOException {
        String extension = FilenameUtils.getExtension(fullFilename);
        logger.debug("Full filename = {}, extension = {}", fullFilename, extension);
        return extension;
    }

    private static String RenameFile(String sourceFilename, String destFilename) throws IOException{
        File sourceFile = new File(sourceFilename);
        File destFile = new File(destFilename);

        String[] fileList = new String[1];

        if (sourceFile.isFile() && !destFile.exists()) {
            fileList = MoveFile(sourceFilename, destFilename);
        }
        else {
            logger.error("RenameFile Source File {} and Dest File {} must be files");
            throw new IOException("RenameFile Source and Destination must be files");
        }

        return fileList[0];
    }

    public static void UnzipFile(String zipFilename, String destinationFolder) throws Exception {
        ZipFile zipFile = new ZipFile(zipFilename);
        zipFile.extractAll(destinationFolder);
        logger.info("UnzipFile filename {} to {} directory", zipFilename, destinationFolder);
    }

    public static void ZipFile(String filename) throws Exception {
        String zipFilename = filename + ".zip";
        ZipFile(filename, zipFilename);
    }

    public static void ZipFile(String filename, String zipFilename) throws Exception {
        ZipFile zipFile = new ZipFile(zipFilename);
        File inputFile = new File(filename);
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        zipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        zipFile.addFile(inputFile, zipParameters);
        logger.info("ZipFile filename {} to {} zipfile", filename, zipFilename);
    }

    public static void ZipFile(String[] filenameList, String zipFilename) throws Exception {
        ZipFile zipFile = new ZipFile(zipFilename);
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        zipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

        ArrayList<File> files = new ArrayList<>();
        for (String filename : filenameList) {
            files.add(new File(filename));
        }

        zipFile.addFiles(files, zipParameters);
        logger.info("ZipFile {} files added to {} zipfile", filenameList.length, zipFilename);
    }

    public static void ZipDirectory(String directoryName) throws Exception {
        ZipDirectory(directoryName, false);
    }

    public static void ZipDirectory(String directoryName, boolean recursive) throws Exception {
        File directory = new File(directoryName);

        if (!directory.isDirectory()) {
            logger.error("ZipDirectory {} is not a directory", directoryName);
        }

        String zipFilename = FilenameUtils.getFullPathNoEndSeparator(directoryName) + ".zip";
        ZipFile zipFile = new ZipFile(zipFilename);

        if (recursive) {
            zipFile.addFolder(directoryName, getDefaultZipParameters());
        }
        else {
            ArrayList<File> nonDirectoryFiles = new ArrayList<File>();
            File[] files = directory.listFiles();
            for (File inputFile : files) {
                if (!inputFile.isDirectory())
                    nonDirectoryFiles.add(inputFile);
            }
            zipFile.addFiles(nonDirectoryFiles, getDefaultZipParameters());
        }

        logger.info("ZipDirectory directory {} added to {} zipfile", directoryName, zipFilename);
    }

    private static ZipParameters getDefaultZipParameters() {
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        zipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        zipParameters.setIncludeRootFolder(false);
        return zipParameters;
    }

    public static boolean FileExists(String filename) {
        File file = new File(filename);
        return (file.exists() && !file.isDirectory());
    }
}
