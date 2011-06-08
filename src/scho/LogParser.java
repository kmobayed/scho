package scho;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;


public class LogParser {

    public void Parse(Jena J, boolean gitLog)
    {
        String CSid = null;
        String tmpP = null;
        String author_email = null;
        String date = null;
        String err = null;
        ChangeSet CS = null;
        Site S = null;
        PullFeed PF = null;
        Integer count=0;
        String cmd;
        
        if (gitLog)
        {
            cmd = "git log --abbrev-commit --parents  --pretty=format:%h%n%p%n%ci%n%ae";

        }
        else
        {
            cmd = "/usr/bin/hg log  --debug --template {rev}:{node}\\n{parents}\\n{date|isodatesec}\\n{author|email}\\n";
        }
 
        try
        {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((CSid = stdInput.readLine()) != null)
            {
                count++;
                if (!gitLog) CSid=CSid.split(":")[1];

                CS=new ChangeSet("CS"+CSid);
                
                if ((tmpP = stdInput.readLine()) !=null)
                {
                    String[] parents;
                    parents=tmpP.split(" ");
                    for (int i =0; i<parents.length; i++)
                    {
                        if (parents[i].equals("-1:0000000000000000000000000000000000000000")) parents[i]="";

                        if (!parents[i].isEmpty()) 
                        {
                            if (!gitLog) parents[i]=parents[i].split(":")[1];
                            CS.addPreviousChgSet("CS" + parents[i]);
                        }
                    }

                    if (tmpP.isEmpty())
                    {
                        String site="S"+CS.getChgSetID().substring(2);
                        S = new Site(site);
                        J.addSite(S);
                    }

                    if (CS.getPreviousChgSet().size()==2)
                    {
                        // pull feed
                        String site="S"+parents[1];
                        S = new Site(site);
                        J.addSite(S);
                        PF= new PullFeed("F"+parents[1]);
                        PF.setHeadPullFeed("CS"+parents[1]);
                        PF.setSite(S.getSiteID());
                        J.addPullFeed(PF);
                        //J.setPullFeed(CS, PF);
                    }
                }

                if ((date = stdInput.readLine()) !=null)
                {
                    Date D;
                    try {
                        SimpleDateFormat sdf1 = new SimpleDateFormat(Main.dateFormatLog);
                        sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
                        D = sdf1.parse(date);
                        SimpleDateFormat sdf2= new SimpleDateFormat(Main.dateFormatJena);
                        sdf2.setTimeZone(TimeZone.getTimeZone("GMT"));
                        date = sdf2.format(D);
                        CS.setDate(date);
                    } catch (ParseException ex) {
                        Logger.getLogger(LogParser.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if ((author_email=stdInput.readLine()) != null)
                {
                    CS.setAuthorEmail(author_email);
                }
                J.addChangeSet(CS);
            }
            while ((err = stdError.readLine()) != null)
            {
                System.out.print("Error :");
                System.out.println(err);
            }
            
        }
        catch (IOException ex)
        {
            Logger.getLogger(LogParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }



    public ChangeSet GetCSdata(String CS,boolean gitLog)
    {
        ArrayList<String> out = new ArrayList<String>();
        String tmp = null;
        String err = null;
        ChangeSet CS1=new ChangeSet();
        try {
           
            String cmd1 ;
            if (gitLog)
            {
                cmd1 = "git show --abbrev-commit --parents --format=%h%n%p%n%s" +CS;
            }
            else
                cmd1 = "/usr/bin/hg log  --debug --template {rev}:{node}\\n{parents}\\n{date|isodatesec}\\n"+CS;

            Process p = Runtime.getRuntime().exec(cmd1);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((tmp = stdInput.readLine()) != null) {
                out.add(tmp);
                
            }

            CS1.setChgSetID(out.get(0));

            String[] tmpP;
            String parents=out.get(1);
            tmpP=parents.split(" ");
            for (int i =0; i<tmpP.length; i++)
            {
                System.out.println(tmpP[i]);
            }
            while ((err = stdError.readLine()) != null) {
                System.out.print("Error :");
                System.out.println(err);
            }
        } catch (IOException ex) {
            Logger.getLogger(LogParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return CS1;
    }


}
