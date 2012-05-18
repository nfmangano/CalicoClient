package calico.plugins.iip.components.canvas;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import calico.CalicoDataStore;
import calico.plugins.iip.components.CIntentionCell;
import calico.plugins.iip.components.CIntentionType;

public class IntentionTypeNameDialog
{
	private static final IntentionTypeNameDialog INSTANCE = new IntentionTypeNameDialog();

	public static IntentionTypeNameDialog getInstance()
	{
		return INSTANCE;
	}

	public enum Action
	{
		OK,
		CANCEL;
	}

	private final JDialog dialog;
	private final Panel panel;

	private Action action;

	private IntentionTypeNameDialog()
	{
		dialog = new JDialog();
		panel = new Panel();

		dialog.setTitle("Enter the name of the intention type");
		dialog.setModal(true);
		dialog.getContentPane().add(panel.dialogPanel);
		dialog.pack();
	}

	public Action queryUserForName(CIntentionType type)
	{
		Rectangle windowBounds = CalicoDataStore.calicoObj.getBounds();
		Rectangle dialogBounds = dialog.getBounds();
		int x = windowBounds.x + ((windowBounds.width - dialogBounds.width) / 2);
		int y = windowBounds.y + ((windowBounds.height - dialogBounds.height) / 2);
		dialog.setLocation(x, y);

		action = Action.CANCEL;

		if (type == null)
		{
			panel.entry.setText("");
		}
		else
		{
			panel.entry.setText(type.getName());
		}
		panel.entry.grabFocus();
		panel.entry.selectAll();
		dialog.setVisible(true);

		return action;
	}

	public Action getAction()
	{
		return action;
	}

	public String getText()
	{
		return panel.entry.getText();
	}

	private void closeDialog(Action action)
	{
		this.action = action;
		dialog.setVisible(false);
	}

	private class EnterAction extends AbstractAction
	{
		@Override
		public void actionPerformed(ActionEvent event)
		{
			closeDialog(Action.OK);
		}
	}

	private class Panel implements ActionListener
	{
		private final JPanel dialogPanel;

		private final JPanel entryPanel;
		private final JTextField entry;

		private final JPanel buttonPanel;
		private final JButton ok;
		private final JButton cancel;

		Panel()
		{
			dialogPanel = new JPanel(new BorderLayout());
			dialogPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

			entryPanel = new JPanel(new BorderLayout(4, 0));
			entryPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
			entry = new JTextField(20);

			buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			ok = new JButton("OK");
			cancel = new JButton("Cancel");

			entryPanel.add(entry, BorderLayout.CENTER);

			buttonPanel.add(ok);
			buttonPanel.add(cancel);

			dialogPanel.add(entryPanel, BorderLayout.CENTER);
			dialogPanel.add(buttonPanel, BorderLayout.SOUTH);

			ok.addActionListener(this);
			cancel.addActionListener(this);

			entry.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
			entry.getActionMap().put("enter", new EnterAction());
		}

		@Override
		public void actionPerformed(ActionEvent event)
		{
			if (event.getSource() == ok)
			{
				closeDialog(Action.OK);
			}
			else if (event.getSource() == cancel)
			{
				closeDialog(Action.CANCEL);
			}
		}
	}
}
