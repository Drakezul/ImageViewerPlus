package de.grueb.crop;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

@SuppressWarnings("serial")
public class ImageVierwerPlus extends JPanel {

	private enum Actions {
		PREVIOUS_PRESSED(KeyStroke.getKeyStroke("pressed LEFT")),
		PREVIOUS_RELEASED(KeyStroke.getKeyStroke("released LEFT")),
		NEXT_PRESSED(KeyStroke.getKeyStroke("pressed RIGHT")),
		NEXT_RELEASED(KeyStroke.getKeyStroke("released RIGHT")),
		CROP(KeyStroke.getKeyStroke("ENTER")),
		SAVE(KeyStroke.getKeyStroke("control S")),
		CANCEL(KeyStroke.getKeyStroke("ESCAPE")),
		ROTATE_CLOCKWISE(KeyStroke.getKeyStroke("UP")),
		ROTATE_ANTI_CLOCKWISE(KeyStroke.getKeyStroke("DOWN")),
		TOGGLE_FULLSCREEN(KeyStroke.getKeyStroke("F11")),
		TOGGLE_SLIDESHOW(KeyStroke.getKeyStroke("F5")),
		OPEN_EXPLORER(KeyStroke.getKeyStroke("SPACE")),
		DELETE_IMAGE(KeyStroke.getKeyStroke("DELETE"));

		private final KeyStroke keyStroke;

		private Actions(KeyStroke keyStroke) {
			this.keyStroke = keyStroke;
		}

		public KeyStroke getKeyStroke() {
			return keyStroke;
		}
	}

	private List<File> images;

	private BufferedImage scaledDrawing;
	private Rectangle currentSelection;
	private Rectangle imageArea;
	private Point startPoint, currentPoint, endPoint;
	private int imageIndex = 0;
	private boolean isBorderless = false;
	private JFrame parentFrame;
	private TimedTask slideShow;
	private TimedTask countdown;

	public ImageVierwerPlus(List<File> images, JFrame parent) {
		this.images = images;
		this.parentFrame = parent;
		setBackground(Color.black);
		this.setSize(new Dimension(parent.getContentPane().getWidth(), parent.getContentPane().getHeight()));
		parent.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				setPreferredSize(
						new Dimension(parent.getContentPane().getWidth(), parent.getContentPane().getHeight()));
			}
		});
		if (images != null && images.size() > 0) {
			updateImage();
			updateTitle();
			addMouseEventListeners();
			addKeyBindings();
		} else {
			addDisposeCountdown(parent);
		}
	}

	public ImageVierwerPlus(List<File> images, JFrame parent, int slideShowInterval) {
		this(images, parent);
		this.slideShow = new TimedTask(slideShowInterval, () -> {
			showNextImage();
		});
	}

	private void showNextImage() {
		getActionForKeyStroke(Actions.NEXT_PRESSED.keyStroke).actionPerformed(null);
		getActionForKeyStroke(Actions.NEXT_RELEASED.keyStroke).actionPerformed(null);
	}

	private void addDisposeCountdown(final JFrame parent) {
		setLayout(new BorderLayout());
		JLabel noImagesFound = new JLabel("No images found", SwingConstants.CENTER);
		noImagesFound.setForeground(Color.GREEN);
		add(noImagesFound, BorderLayout.NORTH);
		int secondsToDispose = 3;
		JLabel timer = new JLabel(secondsToDispose + " seconds until shutdown", SwingConstants.CENTER);
		timer.setForeground(Color.GREEN);
		this.add(timer, BorderLayout.CENTER);

		CountDownLatch latch = new CountDownLatch(secondsToDispose);
		this.countdown = new TimedTask(1, () -> {
			if (latch.getCount() == 0) {
				this.countdown.stop();
				parent.dispatchEvent(new WindowEvent(parent, WindowEvent.WINDOW_CLOSING));
			} else {
				timer.setText(latch.getCount() + " seconds until shutdown");
				latch.countDown();
			}
		});
		this.countdown.start();

		this.repaint();
	}

	private void addMouseEventListeners() {
		this.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				startPoint = forceBounds(event.getPoint());
				currentPoint = null;
				repaint();
			}

			public void mouseReleased(MouseEvent event) {
				if (startPoint != null && currentPoint != null) {
					endPoint = currentPoint;
					currentSelection = GeometryUtils.calculateRectangle(startPoint, endPoint);
					repaint();
				}
			}
		});

		this.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent event) {
				currentPoint = forceBounds(event.getPoint());
				repaint();
			}
		});

	}

	private Point forceBounds(final Point input) {
		return GeometryUtils.forceBounds(input, imageArea.x, imageArea.x + imageArea.width, imageArea.y,
				imageArea.y + imageArea.height);
	}

	private void addKeyBindings() {
		// next image
		addAction(Actions.NEXT_PRESSED, e -> {
			imageIndex++;
			if (imageIndex > images.size() - 1) {
				imageIndex -= images.size();
			}
			updateTitle();
		});
		addAction(Actions.NEXT_RELEASED, e -> {
			updateImage();
		});
		// previous image
		addAction(Actions.PREVIOUS_PRESSED, event -> {
			imageIndex--;
			if (imageIndex < 0) {
				imageIndex += images.size();
			}
			updateTitle();
		});
		addAction(Actions.PREVIOUS_RELEASED, e -> {
			updateImage();
		});
		// crop
		addAction(Actions.CROP, event -> {
			if (currentSelection != null) {
				BufferedImage croppedImage = new BufferedImage(currentSelection.width, currentSelection.height,
						BufferedImage.TYPE_INT_RGB);
				Point newStart = new Point(0, 0);
				Point newEnd = new Point(currentSelection.width, currentSelection.height);
				Point sourceStart = new Point(currentSelection.x - imageArea.x, currentSelection.y - imageArea.y);
				Point sourceEnd = new Point(sourceStart.x + currentSelection.width,
						sourceStart.y + currentSelection.height);
				croppedImage.createGraphics().drawImage(scaledDrawing, newStart.x, newStart.y, newEnd.x, newEnd.y,
						sourceStart.x, sourceStart.y, sourceEnd.x, sourceEnd.y, null);
				updateBoundariesAndOffset(croppedImage);
				scaledDrawing = croppedImage;
				currentPoint = null;
				repaint();
			}
		});
		// save
		addAction(Actions.SAVE, event -> {
			try {
				File imageFile = images.get(imageIndex);
				String type = imageFile.getName().substring(imageFile.getName().lastIndexOf(".") + 1);
				if (!ImageIO.write(scaledDrawing, type, imageFile)) {
					System.out.println("Saving failed");
				} else {
					System.out.println("Saved");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		// reset unsaved change
		addAction(Actions.CANCEL, event -> {
			this.updateImage();
			if (isBorderless) {
				setFullscreen(false);
			}
			if (slideShow != null) {
				slideShow.stop();
			}
		});
		// rotate clockwise
		addAction(Actions.ROTATE_CLOCKWISE, event -> {
			scaledDrawing = rotateImage(scaledDrawing, 90);
			repaint();
		});
		addAction(Actions.ROTATE_ANTI_CLOCKWISE, event -> {
			scaledDrawing = rotateImage(scaledDrawing, -90);
			repaint();
		});
		addAction(Actions.TOGGLE_FULLSCREEN, event -> {
			setFullscreen(!isBorderless);
		});
		addAction(Actions.TOGGLE_SLIDESHOW, event -> {
			if (slideShow != null) {
				if (slideShow.isStopped()) {
					slideShow.start();
				} else {
					slideShow.stop();
				}
			}
		});
		addAction(Actions.OPEN_EXPLORER, event -> {
			try {
				Runtime.getRuntime().exec("explorer.exe /select," + getImageFile().getPath());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});
		addAction(Actions.DELETE_IMAGE, event -> {
			int confirmDelete = JOptionPane.showConfirmDialog(null,
					"Do you really want to delete " + getImageFile().getName() + "?", "Confirm",
					JOptionPane.YES_NO_OPTION);
			if (confirmDelete == JOptionPane.YES_OPTION) {
				boolean trashed = Desktop.isDesktopSupported() && Desktop.getDesktop().moveToTrash(getImageFile());
				if (!trashed) {
					getImageFile().delete();
				}
				images.remove(imageIndex);
				imageIndex--;
				showNextImage();
			}
		});
	}

	private void updateTitle() {
		if (!parentFrame.isUndecorated()) {
			File image = getImageFile();
			String folderName = image.getParent();
			String imageName = image.getName();
			parentFrame.setTitle(folderName + File.separator + imageName);
		}
	}

	private void setFullscreen(boolean enable) {
		Point location = parentFrame.getLocation();
		isBorderless = enable;
		parentFrame.dispose();
		int xFix = parentFrame.getWidth() - parentFrame.getContentPane().getWidth();
		int yFix = parentFrame.getHeight() - parentFrame.getContentPane().getHeight();
		parentFrame = ImageVierwerPlus.getFrame(isBorderless, new Point(location.x + xFix, location.y + yFix));
		parentFrame.add(this);
	}

	private BufferedImage rotateImage(BufferedImage image, int degree) {
		int sourceWidth = image.getWidth();
		int sourceHeight = image.getHeight();
		BufferedImage rotatedImage = new BufferedImage(sourceHeight, sourceWidth, BufferedImage.TYPE_INT_RGB);
		double radiant = Math.toRadians(degree);
		AffineTransform xform = new AffineTransform();
		xform.translate(0.5 * sourceHeight, 0.5 * sourceWidth);
		xform.rotate(radiant);
		xform.translate(-0.5 * sourceWidth, -0.5 * sourceHeight);
		Graphics2D g = (Graphics2D) rotatedImage.getGraphics();
		g.drawImage(image, xform, null);
		g.dispose();
		return rotatedImage;
	}

	private void addAction(Actions actionMapKey, Consumer<ActionEvent> function) {
		InputMap focusedWindowInputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		focusedWindowInputMap.put(actionMapKey.getKeyStroke(), actionMapKey);
		ActionMap focusedWindowActionMap = this.getActionMap();
		focusedWindowActionMap.put(actionMapKey, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent event) {
				function.accept(event);
			}
		});
	}

	private File getImageFile() {
		return images.get(imageIndex % images.size());
	}

	private void updateImage() {
		try {
			currentSelection = null;
			BufferedImage sourceImage = ImageIO.read(getImageFile());
			Rectangle scaledBoundaries = updateBoundariesAndOffset(sourceImage);
			scaledDrawing = new BufferedImage(scaledBoundaries.width, scaledBoundaries.height,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics = scaledDrawing.createGraphics();
			graphics.addRenderingHints(
					new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
			graphics.drawImage(sourceImage, 0, 0, scaledDrawing.getWidth(), scaledDrawing.getHeight(), null);
			currentPoint = null;
			repaint();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Rectangle updateBoundariesAndOffset(Image image) {
		Rectangle scaledBoundaries = getImageScaling(image);
		int xOffset = (getWidth() / 2) - (scaledBoundaries.width / 2);
		int yOffset = (getHeight() / 2) - (scaledBoundaries.height / 2);
		imageArea = new Rectangle(xOffset, yOffset, scaledBoundaries.width, scaledBoundaries.height);
		return scaledBoundaries;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (scaledDrawing != null) {
			final Graphics2D g2d = (Graphics2D) g;
			g2d.drawImage(scaledDrawing, imageArea.x, imageArea.y, null);
			if (currentPoint != null) {
				g2d.setColor(Color.GRAY);
				BasicStroke bs = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5, new float[] { 10 },
						0);
				g2d.setStroke(bs);
				g2d.draw(GeometryUtils.calculateRectangle(startPoint, currentPoint));
			}
		}
	}

	private Rectangle getImageScaling(Image image) {
		double width = image.getWidth(null);
		double height = image.getHeight(null);
		if (width > getWidth()) {
			double ratio = (1.0 * getWidth()) / width;
			width *= ratio;
			height *= ratio;
		}
		if (height > getHeight()) {
			double ratio = (1.0 * getHeight()) / height;
			width *= ratio;
			height *= ratio;
		}
		return new Rectangle(0, 0, (int) width, (int) height);
	}

	public static JFrame getFrame(boolean undecorated, Point location) {
		JFrame frame = new JFrame();
		frame.setUndecorated(undecorated);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(1);
			}

		});
		GraphicsDevice device = frame.getGraphicsConfiguration().getDevice();
		DisplayMode displayMode = device.getDisplayMode();
		frame.setSize(displayMode.getWidth(), displayMode.getHeight());
		if (location != null) {
			frame.setLocation(location);
		}
		frame.setLayout(new BorderLayout());
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
		return frame;
	}

	public static void showOnScreen(int screen, JFrame frame) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gd = ge.getScreenDevices();
		if (screen > -1 && screen < gd.length) {
			frame.setLocation(gd[screen].getDefaultConfiguration().getBounds().x, frame.getY());
		} else if (gd.length > 0) {
			frame.setLocation(gd[0].getDefaultConfiguration().getBounds().x, frame.getY());
		} else {
			throw new RuntimeException("No Screens Found");
		}
	}

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		JFrame frame = getFrame(false, null);
		List<File> images = new ImageFinder(frame).getImages();
		frame.add(new ImageVierwerPlus(images, frame, 3), BorderLayout.CENTER);
		frame.repaint();
	}
}