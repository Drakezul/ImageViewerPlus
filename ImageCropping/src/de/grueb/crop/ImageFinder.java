package de.grueb.crop;

import java.awt.Component;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFileChooser;

public class ImageFinder {

	private static final String[] imageTypes = new String[] { "jpg", "png" };

	private static final FilenameFilter imageFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, final String name) {
			return Arrays.stream(imageTypes).anyMatch(name::endsWith);
		};
	};

	private String currentDirectory;
	private Component parent;

	private List<File> images = new ArrayList<File>(20);

	public ImageFinder(String startDirectory, Component parent) {
		this.currentDirectory = startDirectory;
		this.parent = parent;
		openFileChooserAndListImages();
	}

	public ImageFinder(Component parent) {
		this(System.getProperty("user.home") + "/Desktop", parent);
	}

	public ImageFinder() {
		this(null);
	}

	private void openFileChooserAndListImages() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setCurrentDirectory(new File(currentDirectory));
		fileChooser.setAcceptAllFileFilterUsed(false);
		if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			addImagesToList(fileChooser.getSelectedFile(), true);
		} else {
			System.out.println("No selection");
		}
	}

	private void addImagesToList(File directory, boolean recursive) {
		addImagesToList(directory);
		if (recursive) {
			Arrays.stream(directory.listFiles(File::isDirectory)).forEach((file) -> addImagesToList(file, true));
		}
	}

	private void addImagesToList(File directory) {
		images.addAll(Arrays.asList(directory.listFiles(imageFilter)));
	}

	public List<File> getImages() {
		return images;
	}
}
