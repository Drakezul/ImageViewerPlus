package de.grueb.crop.enums;

import javax.swing.KeyStroke;

import de.grueb.crop.interfaces.HasKeyStroke;
import de.grueb.crop.interfaces.HasPreceding;

public enum KeyBinding implements HasKeyStroke, HasPreceding<KeyBinding> {

	PREVIOUS_PRESSED(KeyStroke.getKeyStroke("pressed LEFT")),
	PREVIOUS_RELEASED(KeyStroke.getKeyStroke("released LEFT"), PREVIOUS_PRESSED),
	NEXT_PRESSED(KeyStroke.getKeyStroke("pressed RIGHT")),
	NEXT_RELEASED(KeyStroke.getKeyStroke("released RIGHT"), NEXT_PRESSED),
	CROP(KeyStroke.getKeyStroke("ENTER")),
	SAVE(KeyStroke.getKeyStroke("control S")),
	CANCEL(KeyStroke.getKeyStroke("ESCAPE")),
	ROTATE_CLOCKWISE(KeyStroke.getKeyStroke("UP")),
	ROTATE_ANTI_CLOCKWISE(KeyStroke.getKeyStroke("DOWN")),
	TOGGLE_FULLSCREEN(KeyStroke.getKeyStroke("F11")),
	TOGGLE_SLIDESHOW(KeyStroke.getKeyStroke("F5")),
	OPEN_EXPLORER(KeyStroke.getKeyStroke("SPACE")),
	DELETE_IMAGE(KeyStroke.getKeyStroke("DELETE"));

	private KeyStroke keyStroke;
	private KeyBinding precedingAction;

	private KeyBinding(KeyStroke keyStroke) {
		this.keyStroke = keyStroke;
	}

	private KeyBinding(KeyStroke keyStroke, KeyBinding counterPart) {
		this(keyStroke);
		this.precedingAction = counterPart;
	}

	@Override
	public KeyStroke getKeyStroke() {
		return keyStroke;
	}

	@Override
	public boolean setKeyStroke(String keyStroke) {
		KeyStroke newStroke = KeyStroke.getKeyStroke(keyStroke);
		if (newStroke != null) {
			this.keyStroke = newStroke;
			return true;
		}
		return false;
	}

	@Override
	public KeyBinding getPreceding() {
		return this.precedingAction;
	}
}
