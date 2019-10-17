import java.awt.BasicStroke;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Date;
import java.util.Calendar;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 
 */
public class AnalisisCuantitativo{
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


    public static int numeroDePuntos=0; //aqui guardaremos cuantos puntos hay en el grafico (cuantas cotizaciones).
    public static ArrayList<Date> arrayFechas = new ArrayList<Date>(); //Aqui guardaremos todas las fechas de las cotizaciones.
    
    public static int[] opsBroker;
    public static double[] cotizaciones;
    
    public static void main(String[] args)throws FileNotFoundException, NoHayDatosException{
        recopilarNombresAbreviaturas();
        
        fechaIni = new Date(2014-1900,1-1,1); //1 de enero de 2014 (los meses van de 0-11)
        fechaFin = new Date(2019-1900,12-1,31); //9 de agosto de 2014 (los meses van de 0-11)
                
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
                
        System.out.println("Brokers con acciones propias: 97-40  48-41  23-47");
        
        Scanner s = new Scanner(System.in);
        while(true){            
            System.out.print("Broker: ");
            int broker = s.nextInt();
            if(broker==0) System.exit(0);
            
            System.out.print("Accion: ");
            int accion = s.nextInt();
             
            brok = brokers.get(broker-1);
            acc = acciones.get(accion-1);
            
            numeroDePuntos=0;
            arrayFechas = new ArrayList<Date>();
                        
            cotizaciones = recogerLasCotizaciones();
            opsBroker = recogerTodasLasOperaciones(0);
            
            analizarDatos(false);
            
            System.out.println("------------------");
        }
    }
    
    public static double[] recogerLasCotizaciones() throws NoHayDatosException, FileNotFoundException{
        //En este arraylist meteremos de momento las cotizaciones, y luego cuando ya sepamos cuantas hay, las pasaremos a un array.
        ArrayList<Double> aux = new ArrayList<Double>();
        
        File f = new File("Historicos\\Acciones\\"+acc+".txt");

        Scanner s = new Scanner(f).useDelimiter("\t");
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        
        while(s.hasNextLine()){
            s.next(); //nos saltamos el nombre de la accion
            double cierre;
            try{
                cierre = Double.parseDouble(s.next().replace(",","."));
            }catch(NumberFormatException e){
                //Esta excepcion saltara cuando el cierre sea un "-", entonces lo que haremos sera saltarnos esa linea de cotizacion.
                s.nextLine();
                System.err.println(acc+"\tHubo una linea de cotizaciones que nos saltamos, porque habia algo fuera de lo normal");
                continue;
            }
            for(int i=1; i<=5; i++) s.next();
            String fecha = s.next();
            s.nextLine();

            Date date = null;
            try {
                date = df.parse(fecha);
            } catch (ParseException e) {e.printStackTrace();}

            if(date.compareTo(fechaIni)>=0 && date.compareTo(fechaFin)<=0){
                if(numeroDePuntos==0) fechaIniCotizaciones=date; //guardarmos el primer dia de cotizaciones.
                aux.add(cierre);
                numeroDePuntos++;
                arrayFechas.add(date);
                fechaFinCotizaciones=date; //vamos actualizando el ultimo dia de cotizaciones.
            }
        } 
        s.close();

        if(numeroDePuntos<=0) throw new NoHayDatosException("No hay datos recopilados para las fechas seleccionadas.");
        
        //COnvertimos el arraylist de cotizaciones en un array normal.
        double[] res = new double[numeroDePuntos];
        for(int i=0; i<aux.size(); i++) res[i] = aux.get(i);
        
        return res;
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
                                }else { 
                                    try{
                                        res[cont++]=0;
                                    }catch(ArrayIndexOutOfBoundsException e){
                                        //Esto salta con las acciones que no tenemos en el archivo de conversor de nombres de BM a VE,
                                        //es decir, cuando el metodo conversorAcciones(), al que se llama al principio,nos devuelve null.
                                        //Lo que haremos entonces es directamente devolver el array res.
                                        System.err.println(acc+"\tCon esta accion nos salimos del array");
                                        return res;
                                    }
                                }                                    
                                s.close();
                            }
                        }
                    }
                }

            }
        }        

        return res;
    }
    
    /**
     * Si la variable booleana impresionCompacta es true, significa que imprimira los datos de un broker sobre una accoion, en una sola linea
     * y separados por tabuladores, para analizarlos luego facilmente en excel.
     */
    public static void analizarDatos(boolean impresionCompacta){
        double[] saldoGanDiarias = new double[numeroDePuntos];
        saldoGanDiarias[0] = 0;
        
        
        int accionesCompradas = 0, accionesVendidas = 0;
        double capitalComprado =0, capitalVendido = 0;
        
        double capitalNegociado = 0;
        double gananciasTotales = 0;
        for(int i=0; i<numeroDePuntos; i++){
            gananciasTotales += (cotizaciones[numeroDePuntos-1]-cotizaciones[i])*opsBroker[i];
            capitalNegociado += Math.abs(opsBroker[i])*cotizaciones[i];
            
            if(opsBroker[i]>0){
                accionesCompradas += opsBroker[i];
                capitalComprado += opsBroker[i]*cotizaciones[i];
            }else{
                accionesVendidas += opsBroker[i];
                capitalVendido += opsBroker[i]*cotizaciones[i];
            }
        }
        double rentabilidad = (gananciasTotales/capitalNegociado);
        
        double precioMedioCompra = capitalComprado/accionesCompradas;
        double precioMedioVenta = capitalVendido/accionesVendidas;
        double difPorcPreciosMedios = (precioMedioVenta-precioMedioCompra)/precioMedioCompra;
        
        DecimalFormat df1 = new DecimalFormat("#,###");
        DecimalFormat df2 = new DecimalFormat("###.##%");
        DecimalFormat df3 = new DecimalFormat("###.###");
        
        if(!impresionCompacta){
            System.out.println("Capital Negociado Total: "+df1.format(capitalNegociado));
            System.out.println("Ganancias Totales: "+df1.format(gananciasTotales));
            System.out.println("Rentabilidad sobre cap. negociado: "+df2.format(rentabilidad));
            System.out.println();        
            System.out.println("Capital comprado: "+df1.format(capitalComprado));
            System.out.println("Capital vendido: "+df1.format(capitalVendido));
            System.out.println("Precio Medio de Compra: "+df3.format(precioMedioCompra));        
            System.out.println("Precio Medio de Venta: "+df3.format(precioMedioVenta));        
            System.out.println("Diferencia porcentual entre precios medios: "+df2.format(difPorcPreciosMedios));
        }
        else{
            System.out.println(acc+"\t"+brok+"\t"+df1.format(capitalNegociado)+"\t"+df1.format(gananciasTotales)+"\t"+df2.format(rentabilidad)+"\t\t"+
                                               df1.format(capitalComprado)+"\t"+df1.format(capitalVendido)+"\t"+
                                               df3.format(precioMedioCompra)+"\t"+df3.format(precioMedioVenta)+"\t"+df2.format(difPorcPreciosMedios));
        }
    }
    
    
    public static void analisisAccionBUCLE()throws FileNotFoundException, NoHayDatosException {
        recopilarNombresAbreviaturas();
        
        fechaIni = new Date(2014-1900,1-1,1); //1 de enero de 2014 (los meses van de 0-11)
        fechaFin = new Date(2019-1900,2-1,25); //9 de agosto de 2014 (los meses van de 0-11)
        
        //menu
        System.out.println("ACCIONES:");
        for(int i=0; i<acciones.size(); i++) System.out.println((i+1)+"\t"+acciones.get(i));
        System.out.println();
        
        Scanner s = new Scanner(System.in);
        while(true){            
            System.out.print("Accion: ");
            int accion = s.nextInt();
            if(accion==0) System.exit(0);
            //fin - menu
            
            acc = acciones.get(accion-1);
            
            numeroDePuntos=0;
            arrayFechas = new ArrayList<Date>();
                        
            cotizaciones = recogerLasCotizaciones();
            
            System.out.println("acc\tbrok\tcapitalNegociado\tgananciasTotales\trentabilidad\t\tcapitalComprado\t"+
                                          "capitalVendido\tprecioMedioCompra\tprecioMedioVenta\tdifPorcPreciosMedios");
                                               
            for(int i=0; i<brokers.size(); i++){
                brok = brokers.get(i);
                opsBroker = recogerTodasLasOperaciones(0);
                
                analizarDatos(true);
            }
            
            System.out.println("------------------");
        }
        
    }
    
    public static void analisisBrokerBUCLE()throws FileNotFoundException, NoHayDatosException {
        recopilarNombresAbreviaturas();
        
        fechaIni = new Date(2014-1900,1-1,1); //1 de enero de 2014 (los meses van de 0-11)
        fechaFin = new Date(2019-1900,12-1,31); //9 de agosto de 2014 (los meses van de 0-11)
        
        //menu
        System.out.println("BROKERS:");
        for(int i=0; i<brokers.size(); i++) System.out.println((i+1)+"\t"+brokersTickers.get(i)+"\t"+brokers.get(i));
        System.out.println();
        
        Scanner s = new Scanner(System.in);
        while(true){             
            System.out.print("Broker: ");
            int broker = s.nextInt();
            if(broker==0) System.exit(0);
            //fin - menu
            
            brok = brokers.get(broker-1);
            
            numeroDePuntos=0;
            arrayFechas = new ArrayList<Date>();
            
            System.out.println("acc\tbrok\tcapitalNegociado\tgananciasTotales\trentabilidad\t\tcapitalComprado\t"+
                                          "capitalVendido\tprecioMedioCompra\tprecioMedioVenta\tdifPorcPreciosMedios");
                                               
            for(int i=0; i<acciones.size(); i++){
                try{
                    acc = acciones.get(i);
                    cotizaciones = recogerLasCotizaciones();
                    opsBroker = recogerTodasLasOperaciones(0);
                    analizarDatos(true);
                }catch(NoHayDatosException e){System.err.println(e);}
                
                numeroDePuntos=0;
                arrayFechas = new ArrayList<Date>();
            }
            
            System.out.println("------------------");
        }
        
    }
}
