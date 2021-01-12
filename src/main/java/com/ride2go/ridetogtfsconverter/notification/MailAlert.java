package com.ride2go.ridetogtfsconverter.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailAlert implements Alert {

	private static final Logger LOG = LoggerFactory.getLogger(MailAlert.class);

	@Autowired
	private JavaMailSender javaMailSender;

	@Override
	public void send(String[] recipients, String subject, String text) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(recipients);
			message.setSubject(subject);
			message.setText(text);
			javaMailSender.send(message);
		} catch (MailParseException e) {
			LOG.error("Problem while parsing the mail message:");
			e.printStackTrace();
		} catch (MailAuthenticationException e) {
			LOG.error("Mail sender authentication failed:");
			e.printStackTrace();
		} catch (MailSendException e) {
			LOG.error("Problem while sending the mail message:");
			e.printStackTrace();
		} catch (MailException e) {
			LOG.error("Mail problem:");
			e.printStackTrace();
		}
	}
}
