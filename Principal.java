import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * 
 */
public class Principal{
    //Aqui guardamos el numero de brokers disonibles que hay, lo cual lo sacamos en el metodo recopilarYcomprobarBrokers.
    public static int numeroDeBrokers=0;

    public static void main(String[] args)throws Exception{
        final long tiempoInicio = System.currentTimeMillis();
        System.out.println("Tiempo de inicio en miliseg: "+tiempoInicio);

        //Recopilamos y comprobamos los brokers.
        recopilarYcomprobarBrokers();

        //Cargamos los tickets de los brokers.
        final String[] brokers = cargarBrokers();

        //Extraemos la informacion de los brokers de VisualEconomy.
        ExtractorDatos.visualEconomy(brokers);

        //Extramos la informacion de las cotizaciones de BolsaMadrid.
        ExtractorDatos.bolsaMadrid();

        System.out.println("SE HAN REALIZADO TODAS LAS ACCIONES CON EXITO! RECUERDA VOLVER A HACER LO MISMO MANYANA!!");

        long tiempoFin = System.currentTimeMillis();
        System.out.println("(Main) Tiempo de finalizacion en miliseg: "+tiempoFin);
        System.out.println("Tiempo empleado en segundos: "+(tiempoFin-tiempoInicio)/1000.0);
    }

    /**
     * Carga los tickers de todos los brokers disponibles en un array.
     */
    public static String[] cargarBrokers()throws Exception{
        File f = new File("Datos\\BrokersActuales.txt");
        Scanner s = new Scanner(f).useDelimiter("\t");

        String[] res = new String[numeroDeBrokers];
        for(int i=0; i<numeroDeBrokers; i++){
            s.next(); //nos saltamos el numero que hay al principio de cada linea
            res[i]=s.next(); //guardamos el ticket del broker.
            s.nextLine(); //saltamos a la linea siguiente
        }
        return res;
    }    

    /**
     * Este metodo recopila el nombre y ticket de cada broker segun visualeconomy, y comprueba que no se halla anyadido ni borrado
     * ningun broker. Si ha habido algun cambio, para el programa y me avisa por si tengo que modificar algo o comprobar que pasa
     * antes de descargar los datos de hoy.
     */
    public static void recopilarYcomprobarBrokers()throws Exception{
        String link = "http://www.visualeconomy.com/MarketMonitor/MarketMonitor.aspx?page=MMO_NEG_BROK_AGENCIES&app=ECO";
        String web = ExtractorDatos.getWeb(link,0);

        //En este archivo guardaremos los tickets y nombres de los brokers disponibles en visualeconomy.
        File f = new File("Datos", "BrokersActuales.txt");
        PrintWriter pw = new PrintWriter(f);

        //Este es el Pattern para sacar el TICKET y el NOMBRE de cada broker.
        Pattern agencia = Pattern.compile("\"Code\">(.*?)\\s*</td>\n.*<td class=\"Descrip\">(.*?)\\s*</td>");
        Matcher m = agencia.matcher(web);

        int i=0;
        while(m.find()) {
            i++;
            String ticket = m.group(1);
            String nombre = m.group(2);
            pw.println(i+"\t"+ticket+"\t"+nombre);
        }
        pw.close();

        //Guardamos el numero de brokers que hay.
        numeroDeBrokers=i;

        //Una vez que ya hemos obtenido los datos de los brokers que hay actualmente en visualeconomy, 
        //vamos a comprobar si son los mismos que habia la ultima vez que descargamos datos.        
        File f2 = new File("Datos\\BrokersAntiguos.txt");
        if(compararFicheros(f, f2)){
            //En caso de que si que sean iguales los brokers de hoy que los de ayer, entonces copiamos el archivo de los brokers de hoy
            //y los renombramos a "BrokersAntiguos" para compararlos con los de manyana.
            copiarFichero(f, f2);
        }
        else{
            
            //Si ha habido algun cambio en los brokers, avisamos y paramos el programa.
            System.out.println("LOS BROKERS NO SON LOS MISMOS DE SIEMPRE, ASI QUE MIRA A VER SI HAY ALGUN PROBLEMA O ALGO QUE ARREGLAR");
            System.out.println("ANTES DE IMPORTAR LOS DATOS DE LAS COMPRAS/VENTAS DE HOY");
            System.exit(0);
        }
    }

    /**
     * Le paso como parametro un archivo como origen y um archivo como destino en donde me copiara el contenido de origen excactamente igual.
     */
    public static void copiarFichero(File origen, File destino) {
        try {
            InputStream in = new FileInputStream(origen);
            OutputStream out = new FileOutputStream(destino);

            byte[] buf = new byte[1024];
            int len;

            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            in.close();
            out.close();
        } catch (IOException ioe){
            ioe.printStackTrace();
        } 
    }

    /**
     * Copiar una carpeta entera, copia el directorio d1 al directorio d2.
     */
    public static void copiarDirectorio(File d1, File d2){
        // Comprobamos que es un directorio
        if (d1.isDirectory()){
            //Comprobamos si existe el directorio destino, si no lo creamos
            if (!d2.exists()){                              
                d2.mkdir();
                System.out.println("Creando directorio " + d2.toString());
            }

            // Sacamos todos los ficheros del directorio
            String[] ficheros = d1.list();
            for (int x=0;x<ficheros.length;x++) {
                // Por cada fichero volvemos a llamar recursivamente a la copa de directorios
                copiarDirectorio(new File(d1,ficheros[x]),new File(d2,ficheros[x]));                           
            }

        } else {
            copiarFichero(d1,d2);
        }
    }

    public static void borrarDirectorio (File directorio){
        File[] ficheros = directorio.listFiles();

        for (int x=0;x<ficheros.length;x++){
            if (ficheros[x].isDirectory()) {
                borrarDirectorio(ficheros[x]);
            }
            ficheros[x].delete();
        }
        
        directorio.delete();
    }

    /**
     * Le paso 2 archivos como parametro y me duvuelve true si son exactamente igual, y si no me da false.
     */
    public static boolean compararFicheros(File f1, File f2) {                    
        try {
            FileReader fr1 = new FileReader(f1);
            FileReader fr2 = new FileReader(f2);

            BufferedReader bf1 = new BufferedReader(fr1);
            BufferedReader bf2 = new BufferedReader(fr2);

            String sCadena1,sCadena2;
            boolean iguales = true;         

            sCadena1 = bf1.readLine();
            sCadena2 = bf2.readLine();

            while ((sCadena1!=null) && (sCadena2!=null) && iguales) {                
                if (!sCadena1.equals(sCadena2)) iguales = false;

                sCadena1 = bf1.readLine();
                sCadena2 = bf2.readLine();
            } 

            if ((iguales) && (sCadena1==null) && (sCadena2==null)) return true;

        } catch (FileNotFoundException fnfe){
            fnfe.printStackTrace();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }

        return false;
    }

    /**
     * Este metodo sirve por si algun dia se asignan mal los nombres de las carpetas de los brokers que van por fechas, y hay que cambiarles
     * los nombres a las carpetas generadas.
     */
    public static void cambiarNombreCarpetas()throws Exception{
        recopilarYcomprobarBrokers();
        String[] brokers = cargarBrokers();
        for(int i=0; i<brokers.length;i++){
            //Primero copiamos el archivo a la nueva carpeta
            File original = new File("Historicos\\Brokers\\"+brokers[i]+"\\2014\\02\\04\\"+brokers[i]+"-20140204.txt");            
            File dir = new File("Historicos\\Brokers\\"+brokers[i]+"\\2014\\02\\03");
            dir.mkdirs();
            File nuevo = new File(dir,brokers[i]+"-20140203.txt");
            nuevo.createNewFile();
            copiarFichero(original,nuevo);

            //Y ahora borramos el archivo y la carpeta antiguos.
            boolean res =original.delete();
            File dir2=new File("Historicos\\Brokers\\"+brokers[i]+"\\2014\\02\\04");
            res =dir2.delete();
        }
    }

    public static void extraerDeCarpetas()throws Exception{
        recopilarYcomprobarBrokers();
        String[] brokers = cargarBrokers();
        for(int i=0; i<brokers.length;i++){
            File dir = new File("Historicos\\Brokers\\"+brokers[i]+"\\2014\\02");
            File[] carpetas = dir.listFiles();
            for(int j=0; j<carpetas.length; j++){
                //if(carpetas[i].isDirectory()){
                //Primero copiamos el archivo a la nueva carpeta
                File[] fich = carpetas[j].listFiles();
                File nuevo = new File(dir,fich[0].getName());
                nuevo.createNewFile();
                copiarFichero(fich[0],nuevo);

                //Y ahora borramos el archivo y la carpeta antiguos.
                boolean res = fich[0].delete();
                res = carpetas[j].delete();
                //}
            }            
        }
    }

    public static void eliminarUnaFechaBrokers()throws Exception{
        recopilarYcomprobarBrokers();
        String[] brokers = cargarBrokers();
        for(int i=0; i<brokers.length;i++){
            File f = new File("Historicos\\Brokers\\"+brokers[i]+"\\2014\\07\\"+brokers[i]+"-20140702.txt");
            f.delete();           
        }
    }

    public static void eliminarLineasDuplicadasAcciones()throws Exception{
        File dir = new File("Historicos\\Acciones");
        File[] ficheros = dir.listFiles();

        for(int i=0; i<ficheros.length; i++){
            //Creamos un fichero temporal en el que iremos escribiendo las lineas que no estan repetidas.
            File tempFile = new File(ficheros[i].getAbsolutePath() + ".tmp");
            PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

            Scanner s = new Scanner(ficheros[i]);

            String lineaAnt = s.nextLine();
            String linea = null;

            //comprobamos que lineas estan repetidas, y esas no las escribimos en el fichero temporal.
            while(s.hasNextLine()){
                linea = s.nextLine();
                if(!lineaAnt.equals(linea)) pw.println(lineaAnt);
                lineaAnt = linea;
            }
            pw.println(lineaAnt);

            pw.close();
            s.close();

            //Eliminamos el fichero antiguo y renombramos el fichero temporal al nombre original.
            if(ficheros[i].delete()) tempFile.renameTo(ficheros[i]);
            else System.out.println("No se ha podido eliminar (modificar) el archivo: "+ficheros[i].getName());
        }
    }
    
    public static void eliminarFECHASDuplicadasAcciones()throws Exception{
        File dir = new File("Historicos\\Acciones");
        File[] ficheros = dir.listFiles();

        for(int i=0; i<ficheros.length; i++){
            //Creamos un fichero temporal en el que iremos escribiendo las lineas que no estan repetidas.
            File tempFile = new File(ficheros[i].getAbsolutePath() + ".tmp");
            PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

            Scanner s = new Scanner(ficheros[i]);

            String lineaAnt = s.nextLine();
            String linea = null;

            //comprobamos que lineas estan repetidas, y esas no las escribimos en el fichero temporal.
            while(s.hasNextLine()){
                linea = s.nextLine();                
                String[] datosAnt = lineaAnt.split("\t");
                String[] datos = linea.split("\t");
                //comprobamos si la fecha es igual, en vez de comprobar si toda la linea es igual.
                //if(!lineaAnt.equals(linea)) pw.println(lineaAnt);
                if(!datosAnt[7].equals(datos[7])) pw.println(lineaAnt);
                lineaAnt = linea;
            }
            pw.println(lineaAnt);

            pw.close();
            s.close();

            //Eliminamos el fichero antiguo y renombramos el fichero temporal al nombre original.
            if(ficheros[i].delete()) tempFile.renameTo(ficheros[i]);
            else System.out.println("No se ha podido eliminar (modificar) el archivo: "+ficheros[i].getName());
        }
    }

    /**
     * Imprime las fechas de una accion y de un broker para ver si algun dia se recopilaron los datos de uno y no de otro.
     * Si efectivamente un dia se recopilaron los datos delos brokers y no de las acciones, hay que ejecutar el metodo llamado:
     * eliminarUnaFechaBrokers(), y eliminar la fecha que no esta recopilada, para que cuadren los datos.
     */
    public static void imprimirFechas()throws Exception{
        File a = new File("Historicos\\Acciones\\ACCIONA.txt");
        Scanner s = new Scanner(a).useDelimiter("\t");

        while(s.hasNextLine()){
            s.next(); //nos saltamos el nombre de la accion
            double cierre = Double.parseDouble(s.next().replace(",","."));
            for(int i=1; i<=5; i++) s.next();
            String fecha = s.next();
            s.nextLine();
            System.out.println(fecha);
        }

        System.out.println("----------");

        File dir = new File("Historicos\\Brokers\\BST MA");
        File[] anyos = dir.listFiles();

        for(int j=0; j<anyos.length; j++){                
            File[] meses = anyos[j].listFiles();
            for(int k=0; k<meses.length; k++){
                File[] dias = meses[k].listFiles();
                for(int m=0; m<dias.length; m++){
                    System.out.println(dias[m].getName());
                }
            }
        }
    }

    /**
     * Este metodo es para extraer de la estructura de carpetas los historicos, tanto de las acciones como de los brokers, que esten
     * comprendidos entre las fecha inicial y la fecha final. Esto es por si un dia o dias se me olvida o no puedo ejecutar el programa
     * pero algun compañero si que ha conseguido ejecutarlo en su ordenador, entonces para no copiar toda la carpeta de historicos entera,
     * ejecutamos este metodo y se nos genenrara una carpeta llamada "HistoricosExtraidos" solo con los historicos de los dias seleccionados
     * y luego hay otro metodo que se llama "InsertarHistoricos" con el que insertaremos en la estrtuctura de carpetas original dichos
     * historicos que hemos extraido.
     */
    public static void extraerHistoricos()throws IOException{
        int anyoIni = 2014;
        int mesIni = 9;
        int diaIni = 23;

        int anyoFin = 2014;
        int mesFin = 9;
        int diaFin = 27;

        Calendar fechaIni = new GregorianCalendar(anyoIni, mesIni-1, diaIni);
        Calendar fechaFin = new GregorianCalendar(anyoFin, mesFin-1, diaFin);

        File carpetaSalida = new File("HistoricosExtraidos");

        File dir = new File("Historicos\\Brokers");
        File[] brokers = dir.listFiles();

        for(int i=0; i<brokers.length; i++){
            File[] anyos = brokers[i].listFiles();

            for(int j=0; j<anyos.length; j++){
                int anyo = Integer.parseInt(anyos[j].getName());
                if(anyo>=anyoIni && anyo<=anyoFin){                    
                    File[] meses = anyos[j].listFiles();

                    for(int k=0; k<meses.length; k++){
                        int mes = Integer.parseInt(meses[k].getName());
                        Calendar fec = new GregorianCalendar(anyo, mes-1, 1);

                        if(fec.compareTo(fechaFin)<=0){
                            File[] dias = meses[k].listFiles();

                            for(int m=0; m<dias.length; m++){
                                String nombre = dias[m].getName();
                                String diaStr = nombre.substring(nombre.length()-6, nombre.length()-4);
                                int dia = Integer.parseInt(diaStr);

                                fec = new GregorianCalendar(anyo, mes-1, dia);
                                if(fec.compareTo(fechaIni)>=0 && fec.compareTo(fechaFin)<=0){
                                    //Si llegamos hasta aqui es que el fichero esta dentro del rango de fechas que queremos, asi que lo copiamos.
                                    File org = dias[m];
                                    //Primero tenemos que crear la estructura de carpetas dentro de la carpeta "HistoricosExtraidos"
                                    File carpDest = new File(carpetaSalida, dias[m].getParent());                                    
                                    carpDest.mkdirs();
                                    //Y luego ya crear un archivo vacio con el nombre correspondiente dentro de la estructura.
                                    File copia = new File(carpDest, dias[m].getName());
                                    copia.createNewFile();
                                    //Y finalmente copiar el contenido del fichero original, al nuevo fichero creado.
                                    copiarFichero(org, copia);                                    
                                }
                            }
                        }
                    }    
                }
            }        
        }

        //Ahora copiamos la carptea entera de los historicos de las cotizaciones de las acciones.
        File carpetaOrg = new File("Historicos\\Acciones");
        File carpetaDes = new File(carpetaSalida, "Historicos\\Acciones");
        copiarDirectorio(carpetaOrg, carpetaDes);
    }

    /**
     * Este metodo sirve para insertar los historicos extraidos con el metodo "extraerHistoricos" en la estructura de carpetas original, junto
     * al resto de historicos que ya teniamos recopilados.
     * PARA EJEUCTARLO HAY QUE PONER LA CARPETA QUE NOS GENERARA EL METODO MENCIONADO ANTES ("HistoricosExtraidos") EN EL DIRECTORIO RAIZ
     * DEL PROGRAMA.
     */
    public static void insertarHistoricos(){
        File carpetaHistoricosExtraidos = new File("HistoricosExtraidos\\temp");
        File carpetaDelRestoDeHistoricos = new File("temp"); //Historicos
        copiarDirectorio(carpetaHistoricosExtraidos, carpetaDelRestoDeHistoricos);

        //Ahora preguntamos si, una vez ya copiados los historicosExtraidos, desea borrar la carpeta de HistoricosExtraidos.
        System.out.println("Ya se han copiado los historicos que habia en la carpeta: \"HistoricosExtraidos\", desea borrar dicha carpeta? (1\\0)");
        Scanner s = new Scanner(System.in);
        int res = s.nextInt();
        if(res==1){
            File dir = new File("HistoricosExtraidos");
            borrarDirectorio(dir);
        }
    }
}
