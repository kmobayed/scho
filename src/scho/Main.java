package scho;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Main {

    public static final boolean GIT_LOG=true;
    public static final boolean Mercurial_LOG=false;
    public static final String dateFormatLog="yyyy-MM-dd HH:mm:ss Z";
    public static final String dateFormatJena="yyyy-MM-dd'T'HH:mm:ss'Z'";

    public static void main( String[] args ) throws IOException, ParseException
    {
        if (args.length<4)
        {
                System.err.println("Usage: java -jar scho.jar <TDB folder> <step in seconds> <log_type> <output_file> [start_date]");
                System.exit(0);
        }

        long startTime = System.currentTimeMillis();
        String DBdirectory = args[0] ;
        LogParser G = new LogParser();
        Jena J= new Jena(DBdirectory);

        System.out.print("Loading ChangeSets and adding PullFeeds ... ");
        String logtype= args[2];

        
        if (logtype.equalsIgnoreCase("git"))
        {
            G.Parse(J,GIT_LOG);
            
        }
        else
        {
            G.Parse(J,Mercurial_LOG);
        }
        System.out.println("DONE");
        System.out.print("Adding PushFeeds ... ");
        J.addPushFeeds();
        System.out.println("DONE");
        long endTime = System.currentTimeMillis();
        System.out.println("Ontology population time (seconds):"+ (endTime-startTime)/1000);

        //print project stats #commit #sites #merge #duration
        System.out.println("Number of site(s) = "+J.getSiteCount());
        System.out.println("Number of commit(s) = "+J.getCommitCount());
        System.out.println("Number of merge(s) = "+J.getMergeCount());
        System.out.println("===========");

        startTime = System.currentTimeMillis();
        Main.calculateDA(J,args);
        endTime = System.currentTimeMillis();
        System.out.println("Divergence awareness calcualtion time (seconds):"+ (endTime-startTime)/1000);
        //J.dump();
        J.close();

    }

    public static void calculateDA(Jena J, String[] args) throws ParseException, FileNotFoundException, IOException
    {
        FileHandler hand = new FileHandler(args[3]);
        hand.setFormatter(new LoggingSimpleFormatter());
        Logger log = Logger.getLogger("scho_log");
        log.addHandler(hand);
        LogRecord rec2 =null;
        String date;
        if (args.length==5)
        {
            date=args[4]; ////for cakePHP "2009-01-01T00:00:00Z";//
        }else
        {
            ChangeSet FCS=J.getFirstCS();
            date = FCS.getDate(); 
        }
        Date D;
        SimpleDateFormat sdf1 = new SimpleDateFormat(dateFormatJena);
        sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
        D = sdf1.parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.setTime(D);
        int step=Integer.valueOf(args[1]);//86400; //in seconds

        ArrayList <ChangeSet> AL2=new ArrayList <ChangeSet>();
        boolean more=true;
//        FileOutputStream fos = new FileOutputStream(args[3]);
//        PrintWriter out = new PrintWriter(fos);

        while (more)
        {
            AL2=J.getCStillDate(cal.getTime());
            System.out.println("Divergence awareness at " + cal.getTime().toString());
            int RM=0;
            int LM=0;
            // calculate divergence at time t
            for (ChangeSet o : AL2)
            {
                //o.print();
                if (!o.isPublished())
                {
                    if (J.inPushFeed(o,cal.getTime()))
                    {
                        o.publish();
                        J.publishChangeSet(o);
                        System.out.println("published : "+o.getChgSetID());
                    }
                    else
                    {
                        if (J.inPullFeed(o,cal.getTime()))
                        {
                            if (J.isPullHead(o,cal.getTime())) //pull head
                            {
                                //publish parents
                                System.out.println("remotely modified: "+o.getChgSetID());
                                RM++;
                                Date D2=sdf1.parse(J.getNextCS(o.getChgSetID()).get(0).getDate());
                                if (D2.before(cal.getTime()))
                                {
                                    J.publishParents(o,cal.getTime());
                                    J.publishChangeSet(o);
                                }
                            }
                            else
                            {
                                System.out.println("remotely modified: "+o.getChgSetID());
                                RM++;
                            }
                        }
                        else
                        {
                            System.out.println("locally modified: "+ o.getChgSetID());
                            LM++;
                        }
                    }

                    if (J.getNextCS(o.getChgSetID()).isEmpty())
                    {
                        more = false;
                    }
                }
                else System.out.println("published : "+o.getChgSetID());
            }

            if (RM>0) System.out.println("Remotely Modified = "+RM);
            if (LM>0) System.out.println("Locally Modified = "+LM);
            if (LM==0 && RM==0) System.out.println("Up-to-date");
//            if (logoutput.equalsIgnoreCase("debug"))
//            {
            rec2 = new LogRecord(Level.INFO,cal.getTime().getTime()+"\t"+LM+"\t"+RM);
            hand.publish(rec2);
//            }
//            out.print(cal.getTime().getTime()+"\t"+LM+"\t"+RM+"\n");
            cal.add(Calendar.SECOND, step);
        }
        hand.close();
//        out.close();
    }
}
