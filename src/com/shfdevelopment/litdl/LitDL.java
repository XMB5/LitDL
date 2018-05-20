package com.shfdevelopment.litdl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.filters.ResponseFilterAdapter;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LitDL {

	private static final Logger LOGGER = LoggerFactory.getLogger(LitDL.class);
	private static final String INSTALL_CA_HOST = "neverssl.com";
	private static final int DEFAULT_PORT = 5678;
	
	public static void main (String[] args) throws IOException {
		
		Options options = new Options();
		Option patternOpt = new Option("pattern", true, "the mime type regex");
		patternOpt.setRequired(true);
		Option portOpt = new Option("port", true, "http proxy port");
		portOpt.setType(Number.class);
		Option logOpt = new Option("log", false, "debug logging");
		options.addOption(logOpt);
		options.addOption(patternOpt);
		options.addOption(portOpt);
		
		int port;
		boolean log;
		Pattern pattern;
		
		try {
			CommandLine cmd = new DefaultParser().parse(options, args);
			port = cmd.hasOption("port") ? ((Number)cmd.getParsedOptionValue("port")).intValue() : DEFAULT_PORT;
			String patternStr = cmd.getOptionValue("pattern");
			pattern = Pattern.compile(patternStr);
			log = cmd.hasOption("log");
		} catch (Exception e) {
			
			System.err.println(e.getMessage());
			System.setOut(System.err);
			new HelpFormatter().printHelp("litdl", options);
			
			System.exit(2);
			return;
		}
		
		if (!log) {
			//lazy, but it works
			System.err.close();
		}
		
		final byte[] utf8Pub = loadUtf8Pub();
		
		BrowserMobProxyServer proxy = new BrowserMobProxyServer();
		proxy.setUseEcc(true);
		proxy.addLastHttpFilterFactory(new ResponseFilterAdapter.FilterSource((response, contents, messageInfo) -> {
            String ctype = response.headers().get("Content-Type");
            if (ctype != null) {
                Matcher m = pattern.matcher(ctype);
                if (m.matches()) {
                    System.out.println(messageInfo.getOriginalUrl());
                }
            }
        }, -1));
		proxy.addRequestFilter((request, contents, messageInfo) -> {
            if (request.getMethod().equals(HttpMethod.GET) && request.headers().get("Host").equals(INSTALL_CA_HOST)) {
                LOGGER.info("sending root ca");
                ByteBuf buf = Unpooled.wrappedBuffer(utf8Pub);
                HttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
                HttpHeaders.setHeader(res, "Content-Type", "application/x-x509-ca-cert");
                HttpHeaders.setHeader(res, "Content-Disposition", "filename=ca-certificate-ec.cer");
                HttpHeaders.setContentLength(res, utf8Pub.length);
                return res;
            }
            return null;
        });
		proxy.start(port);
		
	}
	
	private static byte[] loadUtf8Pub () throws IOException {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream in = LitDL.class.getResourceAsStream("/sslSupport/ca-certificate-ec.cer");
		byte[] buf = new byte[8192];
		int amountRead;
		while ((amountRead = in.read(buf)) != -1) {
			baos.write(buf, 0, amountRead);
		}
		return baos.toByteArray();
		
	}
	
}
