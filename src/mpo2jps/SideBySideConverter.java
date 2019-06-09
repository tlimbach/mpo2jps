package mpo2jps;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class SideBySideConverter {

    public SideBySideConverter(Mpo2Jps callBack, File mpoFile) throws IOException {

        String name = mpoFile.getAbsolutePath().substring(0,
                mpoFile.getAbsolutePath().length() - 4);

        // neuen Namen bestimmen
        name += ".jpg";
        File outputfile = new File(name);

        if (outputfile.exists()) {
            callBack.processingDone(true);
            return;
        }

        // Image Dateien lesen
        ImageReader reader = ImageIO.getImageReadersByFormatName("jpeg")
                .next();
        ImageReadParam param = reader.getDefaultReadParam();
        BufferedImage imgFile1;
        BufferedImage imgFile2;
        try (ImageInputStream iis = ImageIO.createImageInputStream(mpoFile)) {
            reader.setInput(iis, true);
            imgFile1 = reader.read(0, param);
            // Skip the NULL bytes between images.
            while (iis.read() == 0);
            iis.seek(iis.getStreamPosition() - 1);
            // Restart the reader at this new file position.
            reader.reset();
            reader.setInput(iis, true);
            imgFile2 = reader.read(0, param);
        }

        BufferedImage joinedImg = joinBufferedImage(imgFile1, imgFile2);
        ImageIO.write(joinedImg, "jpg", outputfile);

        callBack.processingDone(false);

    }

    public static BufferedImage joinBufferedImage(BufferedImage img1,
            BufferedImage img2) {
        int offset = 2;
        int width = img1.getWidth() + img2.getWidth() + offset;
        int height = Math.max(img1.getHeight(), img2.getHeight()) + offset;
        BufferedImage newImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = newImage.createGraphics();
        g2.drawImage(img1, null, 0, 0);
        g2.drawImage(img2, null, img1.getWidth() + offset, 0);
        g2.dispose();
        return newImage;
    }
}
