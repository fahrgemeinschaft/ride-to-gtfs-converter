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
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

@Service
public class MailAlert implements Alert {

	private static final Logger LOG = LoggerFactory.getLogger(MailAlert.class);

	@Autowired
	private JavaMailSender javaMailSender;

	@Override
	public void send(String[] recipients, String subject, String text) {
		if (recipients == null || recipients.length == 0) {
			LOG.error("No mail recipient addresses found for notification");
			return;
		}
		if (javaMailSender == null
				|| ((JavaMailSenderImpl) javaMailSender).getUsername() == null
				|| ((JavaMailSenderImpl) javaMailSender).getUsername().trim().isEmpty()) {
			LOG.error("No mail sender address found for notification");
			return;
		}
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(recipients);
			message.setSubject(subject);
			message.setText(text);
			javaMailSender.send(message);
			LOG.info("Notification mail has been send");
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
