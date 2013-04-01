package java2cosmosGUI;

import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.Dimension;
import javax.swing.JLabel;
import java.awt.Rectangle;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextArea;

import adapter.impl.ComponentFactory;
import adapter.spec.prov.IExportToCosmos;
import adapter.spec.prov.IManager;

public class J2CGUI extends JFrame {
	
	private javax.swing.JFileChooser jFileChooser = null;  //  @jve:decl-index=0:visual-constraint="130,706"
	
	private javax.swing.JFileChooser jFileMultiChooser = null;
	
	private static final long serialVersionUID = 1L;

	private final String JAVA2COSMOSLOG = "java2cosmosLog.txt";
	
	private JPanel jContentPane = null;

	private JLabel jLabel = null;

	private JLabel jLabel1 = null;

	private JButton jButton = null;

	private JTextField jTextField = null;

	private JLabel jLabel2 = null;

	private JTextField jTextField1 = null;

	private JRadioButton jRadioButton = null;

	private JRadioButton jRadioButton1 = null;

	private JLabel jLabel3 = null;

	private JLabel jLabel4 = null;

	private JLabel jLabel5 = null;

	private JButton jButton1 = null;

	private JTextField jTextField2 = null;

	private JLabel jLabel6 = null;

	private JButton jButton2 = null;

	private JTextField jTextField3 = null;

	private JButton jButton3 = null;

	private JLabel jLabel7 = null;

	private JButton jButton4 = null;

	private JTextField jTextField4 = null;

	private JTextArea jTextArea = null;

	private JLabel jLabel8 = null;

	private JButton jButton5 = null;

	private JTextField jTextField5 = null;

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setBounds(new Rectangle(81, 93, 95, 21));
			jButton.setText("Choose...");
		}
		jButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				loadFile( jTextField );
			}
		});
		return jButton;
	}

	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
			jTextField.setBounds(new Rectangle(183, 95, 463, 21));
		}
		return jTextField;
	}

	/**
	 * This method initializes jTextField1	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField1() {
		if (jTextField1 == null) {
			jTextField1 = new JTextField();
			jTextField1.setBounds(new Rectangle(71, 129, 433, 21));
		}
		return jTextField1;
	}

	/**
	 * This method initializes jRadioButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButton() {
		if (jRadioButton == null) {
			jRadioButton = new JRadioButton();
			jRadioButton.setBounds(new Rectangle(97, 165, 21, 21));
		}
		jRadioButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				mutualExclusion(true);
			}
		});
		return jRadioButton;
	}

	private void mutualExclusion(boolean jarMode){
		jRadioButton1.setSelected( !jarMode);
		jRadioButton.setSelected( jarMode );
		jButton1.setEnabled( jarMode );
		jTextField2.setEnabled( jarMode );
		jButton2.setEnabled( jarMode );
		jTextField3.setEnabled( jarMode );
		jButton4.setEnabled( !jarMode );
		jTextField4.setEnabled( !jarMode );
	}
	
	/**
	 * This method initializes jRadioButton1	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButton1() {
		if (jRadioButton1 == null) {
			jRadioButton1 = new JRadioButton();
			jRadioButton1.setBounds(new Rectangle(244, 165, 21, 21));
		}
		jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				mutualExclusion(false);
			}
		});
		return jRadioButton1;
	}

	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setBounds(new Rectangle(67, 209, 95, 23));
			jButton1.setText("Choose...");
		}
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				loadDependencies( jTextField2 );
			}
		});
		return jButton1;
	}

	/**
	 * This method initializes jTextField2	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField2() {
		if (jTextField2 == null) {
			jTextField2 = new JTextField();
			jTextField2.setBounds(new Rectangle(169, 211, 476, 22));
		}
		return jTextField2;
	}

	/**
	 * This method initializes jButton2	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton2() {
		if (jButton2 == null) {
			jButton2 = new JButton();
			jButton2.setBounds(new Rectangle(77, 240, 93, 21));
			jButton2.setText("Choose...");
		}
		jButton2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				loadFile( jTextField3 );
			}
		});
		return jButton2;
	}

	/**
	 * This method initializes jTextField3	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField3() {
		if (jTextField3 == null) {
			jTextField3 = new JTextField();
			jTextField3.setBounds(new Rectangle(178, 242, 467, 21));
		}
		return jTextField3;
	}

	/**
	 * This method initializes jButton3	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton3() {
		if (jButton3 == null) {
			jButton3 = new JButton();
			jButton3.setBounds(new Rectangle(216, 364, 187, 34));
			jButton3.setText("Run Java2Cosmos");
		}
		jButton3.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				runJava2Cosmos();
			}
		});
		return jButton3;
	}

	private void runJava2Cosmos(){
		IManager imanager = ComponentFactory.createInstance();
		IExportToCosmos export = (IExportToCosmos) imanager.getProvidedInterface("IExportToCosmos");
		setupLog();
		/*String destination = "/home/lsd/ra001973/workspace2/adapterCaseStudy/BankDB/";
		String jarFile = "/home/lsd/ra001973/programs/EJBBank/bank/bankejb.jar"; 
		String pack = "org.apache.geronimo.samples.bank.ejb";
		String classpath = "/home/lsd/ra001973/programs/EJBBank/bank/releases/lib/geronimo-ejb_2.1_spec-1.0.1.jar:/home/lsd/ra001973/programs/EJBBank/bank/releases/lib/geronimo-j2ee_1.4_spec-1.1.jar:/home/lsd/ra001973/programs/EJBBank/bank/releases/lib/geronimo-kernel-1.1.1.jar:/home/lsd/ra001973/programs/EJBBank/bank/releases/lib/geronimo-security-1.1.1.jar:/home/lsd/ra001973/programs/EJBBank/bank/releases/lib/openejb-core-2.1.1.jar:/home/lsd/ra001973/programs/EJBBank/bank/releases/lib/cglib-nodep-2.1_3.jar";
		String rules = "/home/lsd/ra001973/workspace2/Java2Cosmos/src/rules/cosmos.drl";*/
		
		String rules = jTextField5.getText();
		String destination = jTextField.getText();
		String pack = jTextField1.getText();
		if( jRadioButton.isSelected() ){
			//jar mode
			String jarFile = jTextField2.getText();
			String classpath = jTextField3.getText();
			export.changeJarToCosmos(destination, jarFile, pack, classpath, rules);
		}
		else{
			//java source mode
			String compDir = jTextField4.getText();
			export.changeJavaSourceToCosmos( compDir, destination, pack, rules);
		}
		showResult();
			
	}
	
	/**
	 * this method delete the log if it already exists, and create a new one
	 *
	 */
	private void setupLog(){
		File file = new File( JAVA2COSMOSLOG );
		if( file.exists() ){
			file.delete();
			boolean createFile;
			try {
				createFile = file.createNewFile();
				if( createFile )
					System.out.println("File "+file.getAbsolutePath() + " created");
				else
					System.out.println("File "+file.getAbsolutePath() + " wasn't created");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	private void showResult(){
		String result = "";
		File file = new File( JAVA2COSMOSLOG );
		try {
			
			BufferedReader br = new BufferedReader( new FileReader( file ));
			String aux2 = br.readLine();
			while( aux2 != null ){
				result += aux2 + "\n";
				//System.out.println("aux2 = " + aux2);
				aux2 = br.readLine();
			
			}
			/*if( res )
				result += "\n\nThe component "+ componentPath +" is COSMOS";
			else
				result += "\n\nThe component "+ componentPath +" is NOT COSMOS";*/
			jTextArea.setText( result );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(result);
	}
	
	
	/**
	 * This method initializes jButton4	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton4() {
		if (jButton4 == null) {
			jButton4 = new JButton();
			jButton4.setBounds(new Rectangle(153, 301, 90, 26));
			jButton4.setText("Choose...");
		}
		jButton4.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				loadFile( jTextField4 );
			}
		});
		return jButton4;
	}

	/**
	 * This method initializes jTextField4	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField4() {
		if (jTextField4 == null) {
			jTextField4 = new JTextField();
			jTextField4.setBounds(new Rectangle(247, 303, 396, 22));
		}
		return jTextField4;
	}

	/**
	 * This method initializes jTextArea	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getJTextArea() {
		if (jTextArea == null) {
			jTextArea = new JTextArea();
			jTextArea.setBounds(new Rectangle(90, 407, 468, 162));
		}
		return jTextArea;
	}

	
	private void loadFile(JTextField jTF ) {
		int state = getJFileChooser( /*ff*/ ).showOpenDialog(this);
		//System.out.println("entrou no loadfile ");
		if (state == JFileChooser.APPROVE_OPTION) {
			File f = getJFileChooser(/*ff */).getSelectedFile();
			
			jTF.setText( f.getAbsolutePath() );
			
			//setTitle(title);
		}
	}
	
	private void loadDependencies(JTextField jTF ) {
		int state = getJFileMultiChooser( ).showOpenDialog(this);
		if (state == JFileChooser.APPROVE_OPTION) {
			File f = getJFileMultiChooser( ).getSelectedFile();
			String str = jTF.getText() + File.pathSeparator + f.getAbsolutePath();
			jTF.setText( str );
			

		}
	}
	
	/**
	 * This method initializes jFileChooser
	 * 
	 * @return javax.swing.JFileChooser
	 */
	private javax.swing.JFileChooser getJFileChooser(/*FileFilter ff*/) {
		if (jFileChooser == null) {
			jFileChooser = new javax.swing.JFileChooser();
			jFileChooser.setMultiSelectionEnabled(false);
			jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			//jFileChooser.setFileFilter( ff );
			//System.out.println("Files and Directories");
		}


		return jFileChooser;
	}
	
	private javax.swing.JFileChooser getJFileMultiChooser() {
		if (jFileMultiChooser == null) {
			jFileMultiChooser = new javax.swing.JFileChooser();
			jFileMultiChooser.setMultiSelectionEnabled(true);
			jFileMultiChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			//jFileChooser.setFileFilter( ff );
			//System.out.println("Files and Directories");
		}
		return jFileMultiChooser;
	}
	
	/**
	 * This method initializes jButton5	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton5() {
		if (jButton5 == null) {
			jButton5 = new JButton();
			jButton5.setBounds(new Rectangle(57, 64, 95, 22));
			jButton5.setText("Choose...");
		}
		jButton5.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				loadFile( jTextField5 );
			}
		});
		return jButton5;
	}

	/**
	 * This method initializes jTextField5	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField5() {
		if (jTextField5 == null) {
			jTextField5 = new JTextField();
			jTextField5.setBounds(new Rectangle(155, 66, 487, 21));
		}
		return jTextField5;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				J2CGUI thisClass = new J2CGUI();
				thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				thisClass.setVisible(true);
				
			}
		});
	}

	/**
	 * This is the default constructor
	 */
	public J2CGUI() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(678, 605);
		this.setContentPane(getJContentPane());
		this.setTitle("JFrame");
		
		// jar mode is default 
		jRadioButton.setSelected( true );
		mutualExclusion( true );
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel8 = new JLabel();
			jLabel8.setBounds(new Rectangle(18, 66, 35, 16));
			jLabel8.setText("rules");
			jLabel7 = new JLabel();
			jLabel7.setBounds(new Rectangle(11, 301, 141, 20));
			jLabel7.setText("Component directory");
			jLabel6 = new JLabel();
			jLabel6.setBounds(new Rectangle(10, 240, 64, 20));
			jLabel6.setText("classpath");
			jLabel5 = new JLabel();
			jLabel5.setBounds(new Rectangle(10, 211, 55, 20));
			jLabel5.setText("JAR File");
			jLabel4 = new JLabel();
			jLabel4.setBounds(new Rectangle(132, 167, 113, 18));
			jLabel4.setText("Java Source Code");
			jLabel3 = new JLabel();
			jLabel3.setBounds(new Rectangle(14, 166, 82, 19));
			jLabel3.setText("Mode:    JAR");
			jLabel2 = new JLabel();
			jLabel2.setBounds(new Rectangle(8, 130, 60, 19));
			jLabel2.setText("package");
			jLabel1 = new JLabel();
			jLabel1.setBounds(new Rectangle(5, 95, 75, 19));
			jLabel1.setText("destination");
			jLabel = new JLabel();
			jLabel.setBounds(new Rectangle(244, 5, 180, 41));
			jLabel.setFont(new Font("Dialog", Font.BOLD, 24));
			jLabel.setText("Java2Cosmos");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(jLabel, null);
			jContentPane.add(jLabel1, null);
			jContentPane.add(getJButton(), null);
			jContentPane.add(getJTextField(), null);
			jContentPane.add(jLabel2, null);
			jContentPane.add(getJTextField1(), null);
			jContentPane.add(getJRadioButton(), null);
			jContentPane.add(getJRadioButton1(), null);
			jContentPane.add(jLabel3, null);
			jContentPane.add(jLabel4, null);
			jContentPane.add(jLabel5, null);
			jContentPane.add(getJButton1(), null);
			jContentPane.add(getJTextField2(), null);
			jContentPane.add(jLabel6, null);
			jContentPane.add(getJButton2(), null);
			jContentPane.add(getJTextField3(), null);
			jContentPane.add(getJButton3(), null);
			jContentPane.add(jLabel7, null);
			jContentPane.add(getJButton4(), null);
			jContentPane.add(getJTextField4(), null);
			jContentPane.add(getJTextArea(), null);
			jContentPane.add(jLabel8, null);
			jContentPane.add(getJButton5(), null);
			jContentPane.add(getJTextField5(), null);
		}
		return jContentPane;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
