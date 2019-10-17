import java.awt.BasicStroke;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Date;
import java.util.Calendar;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RefineryUtilities;   
import org.jfree.data.general.SeriesException;


/**
 * 
 */
public class GraficoBrokerAccionV3diario{
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
    public static Date fechaIniCotizaciones; //esta es el primer dia del que disponemos cotizaciones de la accion "acc".
    public static Date fechaFinCotizaciones; //esta es el ultimo dia del que disponemos cotizaciones de la accion "acc".

    public static JFreeChart chart;
    public static BufferedImage img;
    public static byte[] imgPNG;

    public static int numeroDePuntos=0; //aqui guardaremos cuantos puntos hay en el grafico (cuantas cotizaciones).
    public static ArrayList<Day> arrayFechas = new ArrayList<Day>(); //Aqui guardaremos todas las fechas de las cotizaciones.
    
    public static int[] opsBroker;
    
    static TimeSeries seriesAccion, seriesBrokerSaldo, seriesBrokerDiario;

    public static void main(String[] args)throws FileNotFoundException, NoHayDatosException{
        recopilarNombresAbreviaturas();
        
        fechaIni = new Date(2014-1900,1-1,1); //1 de enero de 2014 (los meses van de 0-11)
        fechaFin = new Date(2015-1900,12-1,31); //9 de agosto de 2014 (los meses van de 0-11)
                
        menu();
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
            String nombreVE = s.next();
            conversorAcciones.put(nombreBM, nombreVE);
        }
        
        s.close();
    }
    
    public static void menu()throws FileNotFoundException, NoHayDatosException{
        System.out.println("1. Selecciona el numero del BROKER que quieres graficar.");
        System.out.println("2. Selecciona el numero de la ACCION que quieres graficar.");
        System.out.println("3. Para salir escribe el 0.");
        System.out.println();
        
        System.out.println("BROKERS:");
        for(int i=0; i<brokers.size(); i++) System.out.println((i+1)+"\t"+brokersTickers.get(i)+"\t"+brokers.get(i));
        System.out.println();
        
        System.out.println("ACCIONES:");
        for(int i=0; i<acciones.size(); i++) System.out.println((i+1)+"\t"+acciones.get(i));
        System.out.println();
                
        System.out.println("Brokers con acciones propias: 97-40  48-41  23-48 80-37 67-34 58-61");
        
        Scanner s = new Scanner(System.in);
        while(true){            
            System.out.print("Broker: ");
            int broker = s.nextInt();
            if(broker==0) System.exit(0);
            
            System.out.print("Accion: ");
            int accion = s.nextInt();
            System.out.println("------------------");
             
            brok = brokers.get(broker-1);
            acc = acciones.get(accion-1);
            
            numeroDePuntos=0;
            arrayFechas = new ArrayList<Day>();
            hacerGrafico();
        }
    }
    
    public static void hacerGrafico() throws FileNotFoundException, NoHayDatosException {
        //Creamos el grafico.
        chart = createChart(createDatasetAccion());
        
        CrosshairDemo1 demo = new CrosshairDemo1("Crosshair Demo 1", chart, seriesAccion, seriesBrokerDiario);
        //demo.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        demo.pack();   
        RefineryUtilities.centerFrameOnScreen(demo);   
        demo.setVisible(true);   
    }

    private static JFreeChart createChart(XYDataset xydataset) throws FileNotFoundException{
        JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(brok+" - "+acc, "Fecha", acc, xydataset, true, false, false);
        XYPlot xyplot = (XYPlot)jfreechart.getPlot();
        
        //sacamos las operaciones del broker para crear los TtimeSeries del saldo de acciones y de acciones CV cada dia.
        opsBroker = recogerTodasLasOperaciones(0);
        
        //Creamos el eje del SALDO de acciones compradas/vendidas.
        NumberAxis numberaxis1 = new NumberAxis(brok+" DIARIO");
        xyplot.setRangeAxis(1, numberaxis1);
        
        XYDataset xydataset1 = createDatasetBrokerDiario();
        xyplot.setDataset(1, xydataset1);
        xyplot.mapDatasetToRangeAxis(1, 1);
          
        //Formato de la linea de los titulos comprados/vendidos por el broker en DIARIO.        
        xyplot.setRenderer(1, new XYBarRenderer()); 
        XYBarRenderer xybarrenderer = (XYBarRenderer)xyplot.getRenderer(1);
        xybarrenderer.setDrawBarOutline(false);
        xybarrenderer.setBarPainter(new StandardXYBarPainter());
        xybarrenderer.setShadowVisible(false);
        
        //Formato de la linea de la cotizacion de la accion.
        java.awt.geom.Ellipse2D.Double double1 = new java.awt.geom.Ellipse2D.Double(-2D, -2D, 4D, 4D);
        XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer)xyplot.getRenderer();
        xylineandshaperenderer.setBaseShapesVisible(true);
        xylineandshaperenderer.setSeriesShape(0, double1);
        
        
        //Crosshairs
        xyplot.setDomainCrosshairVisible(true);   
        xyplot.setDomainCrosshairLockedOnData(true);   
        xyplot.setRangeCrosshairVisible(false);   
        //Fin-crosshairs
        
        ChartUtilities.applyCurrentTheme(jfreechart);
        return jfreechart;
    }

    private static XYDataset createDatasetAccion() throws NoHayDatosException, FileNotFoundException{
        File f = new File("Historicos\\Acciones\\"+acc+".txt");

        Scanner s = new Scanner(f).useDelimiter("\t");
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        seriesAccion = new TimeSeries("Cotizacion de la accion");

        while(s.hasNextLine()){
            s.next(); //nos saltamos el nombre de la accion
            double cierre = Double.parseDouble(s.next().replace(",","."));
            for(int i=1; i<=5; i++) s.next();
            String fecha = s.next();
            s.nextLine();

            Date date = null;
            try {
                date = df.parse(fecha);
            } catch (ParseException e) {e.printStackTrace();}

            if(date.compareTo(fechaIni)>=0 && date.compareTo(fechaFin)<=0){
                if(numeroDePuntos==0){
                    fechaIniCotizaciones=date; //guardarmos el primer dia de cotizaciones.
                    //METEMOS UN ELEMENTO ADICIONAL "INVENTADO" COMO PRIMER ELEMENTO.
                    seriesAccion.add(new Day(date).previous(), new Double(cierre));
                }
                try{
                    seriesAccion.add(new Day(date), new Double(cierre));
                    numeroDePuntos++;
                    arrayFechas.add(new Day(date));
                    fechaFinCotizaciones=date; //vamos actualizando el ultimo dia de cotizaciones.
                }catch(SeriesException e){System.err.println("Fecha duplicada: "+acc+" "+fecha);}
            }
        } 
        s.close();

        if(numeroDePuntos<=0) throw new NoHayDatosException("No hay datos recopilados para las fechas seleccionadas.");

        return new TimeSeriesCollection(seriesAccion);
    }

    private static XYDataset createDatasetBrokerSaldo() throws FileNotFoundException{
        int suma=0;

        seriesBrokerSaldo = new TimeSeries("Nº de acciones del broker SALDO");

        //Metemos en el dia adicional de cotiacion "inventado" que hemos metido, un saldo inicial de 0.
        seriesBrokerSaldo.add((Day)arrayFechas.get(0).previous(), 0);
        
        for (int i=0; i<opsBroker.length; i++){
            suma += opsBroker[i];
            seriesBrokerSaldo.add(arrayFechas.get(i), suma);
        }

        return new TimeSeriesCollection(seriesBrokerSaldo);
    }
    
    private static XYDataset createDatasetBrokerDiario() throws FileNotFoundException{
        seriesBrokerDiario = new TimeSeries("Nº de acciones del broker DIARIO");
        
        //Metemos en el dia adicional de cotiacion "inventado" que hemos metido, simplemente para que todas las series sean del mismo tamanyo.
        seriesBrokerDiario.add((Day)arrayFechas.get(0).previous(), 0);
        
        for (int i=0; i<opsBroker.length; i++){
            seriesBrokerDiario.add(arrayFechas.get(i), opsBroker[i]);
        }

        return new TimeSeriesCollection(seriesBrokerDiario);
    }

    /**
     * Recoge todas las operaciones del broker seleccionado sobre la accion seleccionada.
     * El array que devuvelva tendra tantos datos como dias de cotizacion se tienen de la accion.
     * Si un dia el broker no hizo ninguna operacion sobre esa accion, se pondra un 0.
     * El paramtero tipo vale 0 para coger todas las operaciones, y +1 o -1 solo para las compradoras/vendedoras.
     * @throws FileNotFoundException 
     */
    public static int[] recogerTodasLasOperaciones(int tipo) throws FileNotFoundException{
        int[] res = new int[numeroDePuntos];
        int cont=0;

        //Para hacer los getYear, getMonth, y getDay sobre un date, hay que conrtuit primero un calendar, porque sino no se puede.
        Calendar cal = Calendar.getInstance();
        cal.setTime(fechaIni);
        int anyoIni = cal.get(Calendar.YEAR);
        int mesIni = cal.get(Calendar.MONTH);
        int diaIni = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTime(fechaFin);
        int anyoFin = cal.get(Calendar.YEAR);
        int mesFin = cal.get(Calendar.MONTH);
        int diaFin = cal.get(Calendar.DAY_OF_MONTH);

        //Sacamos el nombre que tiene la accion en VisualEconomy (VE), ya que el nombre que tenemos en la variable "acc" es el de BolsaMadrid (BM).
        String accVE = conversorAcciones.get(acc); 
        
        String ticker = siglas.get(brok);
        File dir = new File("Historicos\\Brokers\\"+ticker);

        File[] anyos = dir.listFiles();

        for(int j=0; j<anyos.length; j++){
            int anyo= Integer.parseInt(anyos[j].getName());
            if(anyo>=anyoIni && anyo<=anyoFin){                    
                File[] meses = anyos[j].listFiles();

                for(int k=0; k<meses.length; k++){
                    int mes= Integer.parseInt(meses[k].getName());
                    Date fec = new Date(anyo-1900, mes-1, 1);
                    if(fec.compareTo(fechaFin)<=0){
                        //if(anyo>anyoIni || (mes>=mesIni && mes<=mesFin)){
                        File[] dias = meses[k].listFiles();

                        for(int m=0; m<dias.length; m++){
                            String nombre = dias[m].getName();
                            String diaStr = nombre.substring(nombre.length()-6, nombre.length()-4);
                            int dia = Integer.parseInt(diaStr);

                            fec = new Date(anyo-1900, mes-1, dia);
                            if(fec.compareTo(fechaIni)>=0 && fec.compareTo(fechaFin)<=0 
                                && fec.compareTo(fechaIniCotizaciones)>=0 && fec.compareTo(fechaFinCotizaciones)<=0){
                                //if(anyo>anyoIni || mes>mesIni || (dia>=diaIni && dia<=diaFin)){
                                Scanner s = new Scanner(dias[m]);
                                String contenido = "";
                                while(s.hasNextLine()) contenido += s.nextLine()+"\n";

                                Pattern p = Pattern.compile("\n"+accVE+"\t(.*?)\t(.*?)\t(.*?)\n");
                                Matcher match = p.matcher(contenido);            
                                if(match.find()){
                                    String st = match.group(3);
                                    int neto = Integer.parseInt(st);
                                    if(tipo==0 || (tipo==1 && neto>0) || (tipo==-1 && neto<0)){
                                        res[cont++]=neto;
                                    }
                                }else { res[cont++]=0; }                                    
                                s.close();
                            }
                        }
                    }
                }

            }
        }        

        return res;
    }

    protected static void guardarGrafico() {
        File fichero = new File("InformesGuardados\\"+brok+"\\"+acc+".png");

        try {
            ChartUtilities.saveChartAsPNG(fichero, chart, 1000, 600);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
}
