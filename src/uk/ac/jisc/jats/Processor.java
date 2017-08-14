package uk.ac.jisc.jats;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class Processor
{
	/**
	 * Main class: run program
	 */
	public static void main(String[] args)
	{
		// Initialise
		Properties props = new Properties();
		try
		{
			FileInputStream fis = new FileInputStream("params.properties");
			props.load(fis);
			fis.close();
		}
		catch (IOException e)
		{
			System.out.println("No properties file found; exiting.");
			return;
		}
		boolean log = true;
		PrintWriter logger = null;
		try
		{
			logger = new PrintWriter(new FileWriter(props.getProperty(
					"log_path") + props.getProperty("log_file"), true), true);
		}
		catch (IOException e)
		{
			log = false;
			System.out.println("Logging cannot be set up");
		}
		if (log) logger.printf("%tc - Processing started\n", new Date());
		HTTPCall h = new HTTPCall();
		XMLParser xp = new XMLParser();
		// Fetch document IDs
		String base_url = props.getProperty("base_url");
		String url = base_url + props.getProperty("url_1") +
				"&pageSize=" + props.getProperty("pageSize");
		String xml = "";
		List<String> ids;
		try
		{
			xml = h.get(url).toString();
			ids = xp.getIDs(xml);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			if (log) logger.close();
			return;
		}
		// Fetch full text packages and process
		String url_2_end = props.getProperty("url_2_end");
		int counter = 0;
		List<String> titles = new ArrayList<String>();
		int max_title_length = 0;
		try
		{
			max_title_length = Integer.valueOf(
					props.getProperty("max_title_length"));
		}
		catch (NumberFormatException e)
		{
			max_title_length = 50;
		}
		LocalDate earliest = LocalDate.MAX;
		LocalDate latest = LocalDate.MIN;
		for (String id : ids)
		{
			url = base_url + id + url_2_end;
			counter++;
			System.out.println("= XML package: " + counter);
			try
			{
				xml = h.get(url).toString();
				SummaryInfo si = xp.processFullText(xml);
				// System.out.println(xml);
				String title = si.getTitle();
				if (title.length() > max_title_length)
				{
					titles.add(title.substring(0, max_title_length) + "...");
				}
				else titles.add(title);
				LocalDate early = si.getPub_earliest();
				if (early.isBefore(earliest)) earliest = early;
				LocalDate late = si.getPub_latest();
				if (late.isAfter(latest)) latest = late;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				if (log) logger.close();
				return;
			}
			System.out.println(); // line space
			if (log) logger.printf("%tc - Package %d processed\n",
					new Date(), counter);
		}
		// Summary block
		System.out.println("= Summary block: " + counter + " packages processed");
		if (earliest.isBefore(LocalDate.MAX))
		{
			String date = String.format("%04d-%02d-%02d", earliest.getYear(),
					earliest.getMonthValue(), earliest.getDayOfMonth());
			System.out.println("Earliest publication date: " + date);
		}
		if (latest.isAfter(LocalDate.MIN))
		{
			String date = String.format("%04d-%02d-%02d", latest.getYear(),
					latest.getMonthValue(), latest.getDayOfMonth());
			System.out.println("Latest publication date: " + date);
		}
		counter = 0;
		for (String title : titles)
		{
			counter++;
			System.out.println(counter + " " + title);
		}
		if (log)
		{
			logger.printf("%tc - Processing finished\n", new Date());
			logger.close();
		}
	}
}
