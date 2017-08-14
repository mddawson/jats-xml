package uk.ac.jisc.jats;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class XMLParser
{
	private final String ID_TAG = "id";
	public List<String> getIDs(String xml) throws IOException, XMLStreamException
	{
		XMLInputFactory factory = XMLInputFactory.newInstance();
		byte[] byteArray = xml.getBytes("UTF-8");
		ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
		XMLEventReader eventReader = factory.createXMLEventReader(inputStream);
		boolean id = false;
		List<String> ids = new ArrayList<String>();
		while (eventReader.hasNext())
		{
			XMLEvent event = eventReader.nextEvent();
			switch (event.getEventType())
			{
			case XMLStreamConstants.START_ELEMENT:
				StartElement startElement = event.asStartElement();
				String sName = startElement.getName().getLocalPart();
				if (sName.equalsIgnoreCase(ID_TAG)) id = true;
				break;
			case XMLStreamConstants.CHARACTERS:
				if (id)
				{
					Characters characters = event.asCharacters();
					ids.add(characters.getData());
					id = false;
				}
				break;
			}
		}
		return ids;
	}
	private final String JOURNAL_TITLE_TAG = "journal-title";
	private final String ISSN_TAG = "issn";
	private final String ARTICLE_ID_TAG = "article-id";
	private final String PUB_DATE_TAG = "pub-date";
	private final String TITLE_GROUP_TAG = "title-group";
	private final String ARTICLE_TITLE_TAG = "article-title";
	public SummaryInfo processFullText(String xml) throws IOException, XMLStreamException
	{
		SummaryInfo si = new SummaryInfo();
		XMLInputFactory factory = XMLInputFactory.newInstance();
		byte[] byteArray = xml.getBytes("UTF-8");
		ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
		XMLEventReader eventReader = factory.createXMLEventReader(inputStream);
		boolean journal_title = false;
		boolean issn = false;
		boolean article_id = false;
		String id_type = "";
		boolean pub_date = false;
		String pub_type = "";
		LocalDate earliest = LocalDate.MAX;
		LocalDate latest = LocalDate.MIN;
		boolean title_group = false;
		boolean article_title = false;
		while (eventReader.hasNext())
		{
			XMLEvent event = eventReader.nextEvent();
			switch (event.getEventType())
			{
			case XMLStreamConstants.START_ELEMENT:
				StartElement startElement = event.asStartElement();
				String sName = startElement.getName().getLocalPart();
				if (sName.equalsIgnoreCase(JOURNAL_TITLE_TAG)) journal_title = true;
				if (sName.equalsIgnoreCase(ISSN_TAG)) issn = true;
				if (sName.equalsIgnoreCase(ARTICLE_ID_TAG))
				{
					article_id = true;
					Iterator<Attribute> attributes = startElement.getAttributes();
					id_type = attributes.next().getValue();
				}
				if (sName.equalsIgnoreCase(PUB_DATE_TAG))
				{
					pub_date = true;
					Iterator<Attribute> attributes = startElement.getAttributes();
					pub_type = attributes.next().getValue();
					if (!pub_type.equals("collection"))
					{
						boolean day = false;
						boolean month = false;
						boolean year = false;
						int d = 0; int m = 0; int y = 0;
						do
						{
							event = eventReader.nextEvent();
							switch (event.getEventType())
							{
							case XMLStreamConstants.START_ELEMENT:
								startElement = event.asStartElement();
								sName = startElement.getName().getLocalPart();
								if (sName.equalsIgnoreCase("day")) day = true;
								if (sName.equalsIgnoreCase("month")) month = true;
								if (sName.equalsIgnoreCase("year")) year = true;
								break;
							case XMLStreamConstants.CHARACTERS:
								Characters characters = event.asCharacters();
								if (day)
								{
									d = Integer.valueOf(characters.getData());
									day = false;
								}
								if (month)
								{
									m = Integer.valueOf(characters.getData());
									month = false;
								}
								if (year)
								{
									y = Integer.valueOf(characters.getData());
									year = false;
								}
								break;
							case XMLStreamConstants.END_ELEMENT:
								EndElement endElement = event.asEndElement();
								String eName = endElement.getName().getLocalPart();
								if (eName.equalsIgnoreCase(PUB_DATE_TAG))
									pub_date = false;
								break;
							}
						}
						while (pub_date);
						String dateOut = String.format("%04d", y);
						if (m > 0)
						{
							dateOut = dateOut + "-" + String.format("%02d", m);
							if (d > 0)
							{
								dateOut = dateOut + "-" + String.format("%02d", d);
								LocalDate date = LocalDate.of(y, m, d);
								if (date.isBefore(earliest)) earliest = date;
								if (date.isAfter(latest)) latest = date;
							}
						}
						System.out.println("Publication date (type " +
								pub_type + "): " + dateOut);
					}
				}
				if (sName.equalsIgnoreCase(TITLE_GROUP_TAG)) title_group = true;
				if (sName.equalsIgnoreCase(ARTICLE_TITLE_TAG) && title_group)
					article_title = true;
				break;
			case XMLStreamConstants.CHARACTERS:
				Characters characters = event.asCharacters();
				if (journal_title)
				{
					System.out.println("Journal title: " + characters.getData());
					journal_title = false;
				}
				if (issn)
				{
					System.out.println("ISSN: " + characters.getData());
					issn = false;
				}
				if (article_id)
				{
					System.out.println("Article ID (" + id_type + "): " +
							characters.getData());
					article_id = false;
				}
				if (article_title)
				{
					si.setTitle(characters.getData());
					System.out.println("Article title: " + characters.getData());
					article_title = false;
				}
				break;
			case XMLStreamConstants.END_ELEMENT:
				EndElement endElement = event.asEndElement();
				String eName = endElement.getName().getLocalPart();
				if (eName.equalsIgnoreCase(TITLE_GROUP_TAG)) title_group = false;
				break;
			}
		}
		si.setPub_earliest(earliest);
		si.setPub_latest(latest);
		return si;
	}
}
