import java.awt.EventQueue;
import javax.swing.JFrame;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import java.awt.Font;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.*;
import java.io.*;

/**
 * 
 */
public class GraficosVentana{
    public static List<String> brokers = new ArrayList<String>(); //hemos metido el (ticker+" - "+nombre) del broker para diferenciar las sucursales.
    public static List<String> brokersTickers = new ArrayList<String>();
    public static List<String> acciones = new ArrayList<String>();
    
    //En esta hastable relacionaremos los nombres de las acciones en la web de BOLSA MADRID con los nombres en la web VISUAL ECONOMY.
    public static Hashtable<String,String> conversorAcciones = new Hashtable<String,String>(156);
    
    //En esta hastable relacionaremos los (ticker+" - "+nombre) de los brokers con sus siglas/abreviatura/ticker. Tambien metemos el ticker en el 
    //primer valor, porque hay brokers que como nombre tienen lo mismo en todas sus sucursales, en cambio los tickers si que los tienen diferentes.
    public static Hashtable<String,String> siglas = new Hashtable<String,String>(145);

    public static Date fechaIni;
    public static Date fechaFin;
    public static String brok; //Aqui guardaremos el broker seleccionado
    public static String acc; //Aqui guardaremos la accion seleccionada
    
    
    private JFrame frame;
    private JComboBox comboBox, comboBox_1;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        //EventQueue.invokeLater(new Runnable() {
        //    public void run() {
                try {
                    //Lo primero recopilamos los nombres de lso brokers y acciones.
                    recopilarNombresAbreviaturas();
                    
                    fechaIni = new Date(2014-1900,1-1,1); //1 de enero de 2014 (los meses van de 0-11)
                    fechaFin = new Date(2019-1900,2-1,31); //9 de agosto de 2014 (los meses van de 0-11)          
                    
                    //Y ahora ya creamos la ventana.
                    GraficosVentana window = new GraficosVentana();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        //    }
        //});
    }

    /**
     * Create the application.
     */
    public GraficosVentana() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
		frame.setBounds(100, 100, 462, 147);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
                
        JLabel label = new JLabel("Broker:");
        label.setFont(new Font("Tahoma", Font.PLAIN, 16));
        label.setBounds(10, 13, 62, 20);
        frame.getContentPane().add(label);
        
        JLabel label_1 = new JLabel("Accion:");
        label_1.setFont(new Font("Tahoma", Font.PLAIN, 16));
        label_1.setBounds(10, 44, 62, 20);
        frame.getContentPane().add(label_1);
        
        comboBox = new JComboBox(brokers.toArray()); //le pasamos los ticker+" - "+nombre de los brokers
        comboBox.setBounds(83, 13, 353, 20);
        frame.getContentPane().add(comboBox);
        
        comboBox_1 = new JComboBox(acciones.toArray()); //le pasamos los nombres de las acciones.
        comboBox_1.setBounds(83, 44, 353, 20);
        frame.getContentPane().add(comboBox_1);
        
        
        JButton btnGenerarGrafico = new JButton("Generar grafico");
        btnGenerarGrafico.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                brok = (String)comboBox.getSelectedItem();
                acc = (String)comboBox_1.getSelectedItem();
                GraficoBrokerAccionV3 graf = new GraficoBrokerAccionV3(fechaIni, fechaFin, brok, acc, brokers, brokersTickers, acciones, conversorAcciones, siglas);
            }
        });
		btnGenerarGrafico.setBounds(149, 75, 171, 23);
        frame.getContentPane().add(btnGenerarGrafico);
    }
    
    public static void recopilarNombresAbreviaturas()throws FileNotFoundException{
        //Leemos los listados de brokers y acciones.
        File f = new File("Datos\\BrokersActuales.txt");
        Scanner s = new Scanner(f).useDelimiter("\t|\r\n");
        try{
            while(s.hasNextLine()){
                String numero = s.next();
                String ticker = s.next();
                String nombre = s.next();
                brokersTickers.add(ticker);
                brokers.add(ticker+" - "+nombre);
                siglas.put(ticker+" - "+nombre, ticker);
            }
        }catch(Exception e){} //ponemos este catch porque al final del arhicvo hay una linea en blanco que hace que pete el programa.
        
        //Leemos los nombres de todas las acciones.
        File dir = new File("Historicos\\Acciones");
        String[] nombres = dir.list();
        for(int i=0; i<nombres.length; i++) acciones.add(nombres[i].replaceAll(".txt",""));
        
        //Leemos el fichero de las conversiones de nombres de las acciones entre BOLSA MADRID y VISUAL ECONOMY.
        f = new File("Datos\\AccionesBMFICHEROStoAccionesVE.txt");
        s = new Scanner(f).useDelimiter("\t|\r\n");
        while(s.hasNextLine()){
            String nombreBM = s.next(); 
            String nombreVE = s.next();//System.out.println(nombreBM+" "+nombreVE);
            conversorAcciones.put(nombreBM, nombreVE);
        }
        
        s.close();
    }
}
