package main;

import java.awt.BorderLayout;
import java.io.File;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.UIManager;

import de.grueb.crop.UI.ImageFinder;
import de.grueb.crop.UI.ImageVierwerPlus;

public class Main {

	/**
	 * args[0] can be the path to the directory e.g. "E:\\exampleFolder"
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		JFrame frame = ImageVierwerPlus.getFrame(false, null);
		List<File> images;
		if (args != null && args.length > 0) {
			images = new ImageFinder(new File(args[0]), frame).getImages();
		} else {
			images = new ImageFinder(frame).getImages();
		}
		frame.add(new ImageVierwerPlus(images, frame, 3), BorderLayout.CENTER);
		frame.repaint();
	}
}
