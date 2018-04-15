package de.grueb.crop.UI;

import java.awt.Component;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFileChooser;

public final class ImageFinder {

	private static final String[] imageTypes = new String[] { "jpg", "png" };

	private static final FilenameFilter imageFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, final String name) {
			return Arrays.stream(imageTypes).anyMatch(name::endsWith);
		};
	};

	/**
	 * Relative position of the FileChooser
	 */
	private Component parent;

	private List<File> images = new ArrayList<File>(20);

	/**
	 * Start filechooser at designated path with given parentComponent
	 * 
	 * @param startDirectory
	 * @param parent
	 *            is used for relative position of the fileChooser
	 */
	public ImageFinder(String startDirectory, Component parent) {
		this.parent = parent;
		openFileChooserAndListImages(startDirectory);
	}

	/**
	 * Start fileChooser on desktop
	 * 
	 * @param parent
	 *            for relative position of the fileChooser
	 */
	public ImageFinder(Component parent) {
		this(System.getProperty("user.home") + "/Desktop", parent);
	}

	/**
	 * Skip filechooser and list images of given folder (and subs)
	 * 
	 * @param folder
	 * @param parent
	 */
	public ImageFinder(File folder, Component parent) {
		this.parent = parent;
		addImagesToList(folder, true);
	}

	public ImageFinder() {
		this(null);
	}

	private void openFileChooserAndListImages(String currentDirectory) {
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

	private void addImagesToList(final File directory, final boolean recursive) {
		addImagesToList(directory);
		if (recursive) {
			Arrays.stream(directory.listFiles(File::isDirectory)).forEach((file) -> addImagesToList(file, recursive));
		}
	}

	private void addImagesToList(final File directory) {
		images.addAll(Arrays.asList(directory.listFiles(imageFilter)));
	}

	public List<File> getImages() {
		return images;
	}
}
