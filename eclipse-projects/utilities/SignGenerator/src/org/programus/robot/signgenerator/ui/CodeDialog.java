package org.programus.robot.signgenerator.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.programus.robot.signgenerator.model.SignData;
import org.programus.robot.signgenerator.utils.UI;

public class CodeDialog extends JDialog {
	/** 用来做对象持久化识别的常数。由于本类不打算做持久化，可以忽略。 */
	private static final long serialVersionUID = 6777389406374017274L;
	
	private enum Type {
		TrafficSign("TrafficSign"),
		Int("int"),
		Byte("byte"),
		Boolean("boolean");
		
		public final String value;
		
		Type(String v) {
			this.value = v;
		}
		
		@Override
		public String toString() {
			return this.value;
		}
	}
	private UI ui = UI.getInstance();

	private SignData data;

	private JTextArea text;
	
	private Type type = Type.TrafficSign;
	private int dim;
	
	public CodeDialog(SignData data) {
		this.data = data;
		this.initComponents();
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.pack();
		this.setLocationRelativeTo(this.getParent());
	}
	
	private void initComponents() {
		Container pane = this.getContentPane();
		pane.setLayout(new BorderLayout());
		this.initTextArea();
		JScrollPane scroll = new JScrollPane(this.text, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pane.add(this.getTypeSelectPanel(), BorderLayout.NORTH);
		pane.add(scroll, BorderLayout.CENTER);
	}
	
	private void initTextArea() {
		this.text = new JTextArea(this.data.getEdgeLen() + 2, this.data.getEdgeLen() * 5 + 10);
		this.text.setTabSize(2);
		this.text.setEditable(false);
		Font font = this.text.getFont();
		Font newFont = new Font(Font.MONOSPACED, font.getStyle(), font.getSize());
		this.text.setFont(newFont);
        resetTextArea();
	}
	
	private JPanel getTypeSelectPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.add(new JLabel(ui.getText("label.type")));
		
		JComboBox<Type> typeCb = new JComboBox<Type>(Type.values());
		panel.add(typeCb);
		
		final JComboBox<String> dimCb = new JComboBox<String>(new String[] {"[]", "[][]"});
		panel.add(dimCb);
		
		dimCb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				@SuppressWarnings("unchecked")
				JComboBox<String> cb = (JComboBox<String>) e.getSource();
				dim = cb.getSelectedIndex();
				resetTextArea();
			}
		});
		
		typeCb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				@SuppressWarnings("unchecked")
				JComboBox<String> cb = (JComboBox<String>) e.getSource();
				type = Type.values()[cb.getSelectedIndex()];
				dimCb.setEnabled(type != Type.TrafficSign);
				resetTextArea();
			}
		});
		
		dimCb.setSelectedIndex(0);
		typeCb.setSelectedIndex(this.type.ordinal());
		return panel;
	}
	
	private void resetTextArea() {
		this.text.setText(this.generateCode());
	}
	
	private String generateCode() {
		final int LF_MASK = 7;
		String lf = System.getProperty("line.separator", "\n");
		StringBuilder sb = new StringBuilder("new ");
		sb.append(type.value);
		if (type != Type.TrafficSign) {
			sb.append("[]");
			if (dim > 0) {
				sb.append("[]");
			}
			sb.append(" {");
			sb.append(lf);
		} else {
			sb.append("(");
		}
		int n = this.data.getEdgeLen();
		int count = 0;
		for (int y = 0; y < n; y++) {
			if (type != Type.TrafficSign) {
				if (dim > 0) {
					sb.append("{");
				}
				sb.append("\t");
			}
			for (int x = 0; x < n; x++) {
				boolean b = this.data.isBlockBlack(x, y);
				if (type == Type.TrafficSign) {
					if (b) {
						if ((count & LF_MASK) == 0) {
							sb.append(lf);
							sb.append('\t');
						}
						sb.append(String.format("0x%02x%02x, ", y, x));
						count++;
					}
				} else {
					if (type == Type.Boolean) {
						sb.append(String.valueOf(b));
					} else {
						if (type == Type.Byte) {
							sb.append("(byte)");
						}
						sb.append(b ? "0x00" : "0xff");
					}
					if (x < n - 1) {
						sb.append(", ");
					}
				}
			}
			if (type != Type.TrafficSign) {
				if (dim > 0) {
					sb.append("}");
				}
				if (y < n - 1) {
					sb.append(", ");
				}
				sb.append(lf);
			}
		}
		if (type != Type.TrafficSign) {
			sb.append("}");
		} else {
			if (count > 0) {
				sb.delete(sb.lastIndexOf(","), sb.length());
				sb.append(lf);
			}
			sb.append(")");
		}
		return sb.toString();
	}
	
	public SignData getData() {
		return this.data;
	}
}
