package de.grueb.crop.UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import de.grueb.crop.interfaces.ConfigurableKeyBinding;
import de.grueb.crop.interfaces.HasKeyStroke;
import de.grueb.crop.interfaces.HasPreceding;

public class KeyConfigurator<E extends Enum<E> & HasKeyStroke & HasPreceding<E>> extends JFrame {

	/** generated */
	private static final long serialVersionUID = 1962545573956314763L;

	private HashMap<JButton, EnumAndLable> map = new HashMap<JButton, EnumAndLable>();

	private FocusListener focusListener = new FocusListener() {

		@Override
		public void focusLost(FocusEvent e) {
			setButtonColor(e, UIManager.getColor("Button.background"));
		}

		@Override
		public void focusGained(FocusEvent e) {
			setButtonColor(e, Color.GREEN);
		}

		private void setButtonColor(FocusEvent event, Color color) {
			if (event.getSource() instanceof JButton) {
				((JButton) event.getSource()).setBackground(color);
			}
		}
	};

	private KeyListener keyListener = new KeyListener() {

		@Override
		public void keyReleased(KeyEvent e) {
			// System.out.println("released");
		}

		@Override
		public void keyPressed(KeyEvent e) {
			// System.out.println("pressed");
			map.get(e.getSource()).label.setText(KeyStroke.getKeyStrokeForEvent(e).toString().replace("pressed", ""));
		}

		@Override
		public void keyTyped(KeyEvent e) {}
	};

	public KeyConfigurator(String title, ConfigurableKeyBinding mainWindow, Class<E> enumeration) {
		super(title);
		this.setLayout(new BorderLayout());
		JPanel mainPanel = new JPanel();
		this.add(mainPanel, BorderLayout.CENTER);
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		E[] enums = enumeration.getEnumConstants();
		List<E> releaseBindings = Arrays.stream(enums).filter((e) -> e.getKeyStroke().isOnKeyRelease())
				.collect(Collectors.toList());
		mainPanel.setLayout(new GridLayout(enums.length - releaseBindings.size() + 1, 2, 5, 5));
		for (E enumE : enums) {
			if (!enumE.getKeyStroke().isOnKeyRelease()) {
				JLabel label = new JLabel(enumE.getKeyStroke().toString());
				// label.setBorder(new EmptyBorder(2, 2, 2, 2));
				JButton button = new JButton(enumE.name());
				// button.setBorder(temp);
				button.addFocusListener(focusListener);
				button.addKeyListener(keyListener);
				map.put(button, new EnumAndLable(enumE, label));
				mainPanel.add(button);
				mainPanel.add(label);
			}
		}
		JPanel acceptWrapper = new JPanel(new FlowLayout()), cancelWrapper = new JPanel(new FlowLayout());
		JButton accept = new JButton("Accept"), cancel = new JButton("Cancel");
		accept.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				for (Entry<JButton, EnumAndLable> entry : map.entrySet()) {
					E toUpdate = entry.getValue().enumE;
					toUpdate.setKeyStroke(entry.getValue().label.getText());
					mainWindow.changeKeyBinding(toUpdate);
				}
				for (E enumE : releaseBindings) {
					enumE.setKeyStroke(enumE.getPreceding().getKeyStroke().toString().replace("pressed", "released"));
					mainWindow.changeKeyBinding(enumE);
				}
			}
		});
		acceptWrapper.add(accept);
		KeyConfigurator<E> _this = this;
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_this.dispatchEvent(new WindowEvent(_this, WindowEvent.WINDOW_CLOSING));
			}
		});
		cancelWrapper.add(cancel);
		mainPanel.add(acceptWrapper);
		mainPanel.add(cancelWrapper);
		this.setVisible(true);
		pack();
		_this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				_this.dispose();
			}

		});
	}

	/**
	 * Wrapper object to remember which JButton corresponds to which enum and to
	 * which Label. JButton could be mapped to enum by text, but the text should not
	 * have to correspond to the enum name (exactly)
	 * 
	 * @author sebas
	 *
	 */
	private class EnumAndLable {

		public E enumE;
		public JLabel label;

		public EnumAndLable(E enumeration, JLabel label) {
			this.enumE = enumeration;
			this.label = label;
		}

	}
}