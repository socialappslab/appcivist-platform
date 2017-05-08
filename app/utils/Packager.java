package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Packager
{
    public static void packZip(File output, List<File> sources, String format) throws IOException
    {
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(output));
        zipOut.setLevel(Deflater.DEFAULT_COMPRESSION);

        for (File source : sources)
        {
            zipFile(zipOut, "", source, format);
        }
        zipOut.flush();
        zipOut.close();
    }

    private static String buildPath(String path, String file)
    {
        if (path == null || path.isEmpty())
        {
            return file;
        } else
        {
            return path + "/" + file;
        }
    }

    private static void zipFile(ZipOutputStream zos, String path, File file, String format) throws IOException
    {
        if (!file.canRead())
        {
            System.out.println("Cannot read " + file.getCanonicalPath() + " (maybe because of permissions)");
            return;
        }

        zos.putNextEntry(new ZipEntry(buildPath(path, file.getName()+format)));

        FileInputStream fis = new FileInputStream(file);

        byte[] buffer = new byte[4092];
        int byteCount = 0;
        while ((byteCount = fis.read(buffer)) != -1)
        {
            zos.write(buffer, 0, byteCount);
            System.out.flush();
        }
        fis.close();
        zos.closeEntry();
    }
}
