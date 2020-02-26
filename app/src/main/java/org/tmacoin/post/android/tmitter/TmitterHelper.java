package org.tmacoin.post.android.tmitter;

import org.tma.peer.thin.Tweet;

public class TmitterHelper {
	


	//public JPanel showBackButton(Tweet tweet) {
		/*JPanel panel = new JPanel(new MigLayout("wrap 2", "[right][fill]"));
		final JButton btnSubmit = new JButton();
		btnSubmit.setAction(new ShowMyTweets(frame, tweet.getRecipient()));
		btnSubmit.setText("Back");
		
		frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "backButton");

		frame.getRootPane().getActionMap().put("backButton", new AbstractAction() {
			private static final long serialVersionUID = 4946947535624344910L;

			public void actionPerformed(ActionEvent actionEvent) {
				btnSubmit.doClick();
				frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).clear();
				frame.getRootPane().getActionMap().clear();
			}
		});
		
		JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEFT));
		flow.add(btnSubmit);
		panel.add(flow);
		panel.add(new JLabel(""));
		return panel;

		 */
	//}


	
	public void createForm(Tweet tweet) {

	/*	JLabel label = new JLabel("Enter reply:");
		panel.add(label);
		
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JTextArea area = new JTextArea(3, 45);
		JTextFieldRegularPopupMenu.addTo(area);
		JScrollPane scroll = new JScrollPane (area);3
		p.add(scroll);
		panel.add(p);
		
		panel.add(new JLabel(""));
		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton btnSubmit = new JButton("Submit");
		btnSubmit.setAction(new SendReplyAction(frame, area, tweet));
		p.add(btnSubmit);
		panel.add(p);

		frame.getRootPane().setDefaultButton(btnSubmit);

	 */
	}

}
