import javax.swing.*;

public class NffvApplication extends JFrame implements PanelContainer{

	public NffvApplication(){
		this.setTitle("Neurotechnology - Nffv Sample");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPanel(new ScannerModules(this));
	}
	
	public void setPanel(JPanel panel){
		setContentPane(panel);
		this.setSize(panel.getPreferredSize());
		setVisible(true);
	}
	
	public static void main(String[] args) {
		new NffvApplication();
	}

}
