package mpo2jps;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.monte.media.exif.EXIFReader;
import org.monte.media.math.Rational;
import org.monte.media.tiff.TIFFField;
import org.monte.media.tiff.TIFFTag;

public class SideBySideConverter {

	public SideBySideConverter(File mpoFile) {

		try {
			String name = mpoFile.getAbsolutePath().substring(0,
					mpoFile.getAbsolutePath().length() - 4);

			// Parallaxe aus EXIF Daten lesen
			double parallaxX = 0.0;
			double parallaxY = 0.0;

			EXIFReader exifReader = new EXIFReader(mpoFile);
			exifReader.read();
			HashMap<TIFFTag, TIFFField> metaDataMap = exifReader
					.getMetaDataMap();
			Set<TIFFTag> set = metaDataMap.keySet();
			TIFFField tiffField = null;
			for (TIFFTag tag : set) {

				if (tag.getName().equals("ParallaxXShift")) {
					tiffField = metaDataMap.get(tag);
					Rational rat = (Rational) tiffField.getData();
					long denominator = rat.getDenominator();
					long numerator = rat.getNumerator();

					try {
						parallaxX = (double) numerator / (double) denominator;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (tag.getName().equals("ParallaxYShift")) {
					tiffField = metaDataMap.get(tag);
					Rational rat = (Rational) tiffField.getData();
					System.out.println("y=" + rat.toDescriptiveString());
					long denominator = rat.getDenominator();
					long numerator = rat.getNumerator();

					try {
						parallaxY = (double) numerator / (double) denominator;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			// neuen Namen bestimmen
			// name += "sat" + saturation + "curve" + darkFactor + "_rc.jpg";
			name += ".jpg";
			File outputfile = new File(name);

			if (outputfile.exists())
				return;

			// Image Dateien lesen
			ImageReader reader = ImageIO.getImageReadersByFormatName("jpeg")
					.next();
			ImageReadParam param = reader.getDefaultReadParam();
			ImageInputStream iis = ImageIO.createImageInputStream(mpoFile);

			reader.setInput(iis, true);
			BufferedImage imgFile1 = reader.read(0, param);
			// Skip the NULL bytes between images.
			while (iis.read() == 0)
				;
			iis.seek(iis.getStreamPosition() - 1);
			// Restart the reader at this new file position.
			reader.reset();
			reader.setInput(iis, true);
			BufferedImage imgFile2 = reader.read(0, param);

			iis.close();

			// ImageIO.write(imgFile1, "jpg", new File(n1));
			// ImageIO.write(imgFile2, "jpg", new File(n2));

			BufferedImage joinedImg = joinBufferedImage(imgFile1, imgFile2);
			ImageIO.write(joinedImg, "jpg", outputfile);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static BufferedImage joinBufferedImage(BufferedImage img1,
			BufferedImage img2) {
		int offset = 2;
		int width = img1.getWidth() + img2.getWidth() + offset;
		int height = Math.max(img1.getHeight(), img2.getHeight()) + offset;
		BufferedImage newImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = newImage.createGraphics();
		// Color oldColor = g2.getColor();
		// g2.setPaint(Color.BLACK);
		// g2.fillRect(0, 0, width, height);
		// g2.setColor(oldColor);
		g2.drawImage(img1, null, 0, 0);
		g2.drawImage(img2, null, img1.getWidth() + offset, 0);
		g2.dispose();
		return newImage;
	}

	// public static BufferedImage joinBufferedImage(BufferedImage img1,
	// BufferedImage img2) {
	// int offset = 2;
	// int width = img1.getWidth() + img2.getWidth() + offset;
	// int height = Math.max(img1.getHeight(), img2.getHeight()) + offset;
	// BufferedImage newImage = new BufferedImage(width, height,
	// BufferedImage.TYPE_INT_ARGB);
	// Graphics2D g2 = newImage.createGraphics();
	// // Color oldColor = g2.getColor();
	// // g2.setPaint(Color.BLACK);
	// // g2.fillRect(0, 0, width, height);
	// // g2.setColor(oldColor);
	// g2.drawImage(img1, null, 0, 0);
	// g2.drawImage(img2, null, img1.getWidth() + offset, 0);
	// g2.dispose();
	// return newImage;
	// }

}
