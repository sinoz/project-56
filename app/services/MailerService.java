package services;

import forms.MailerForm;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;

import javax.inject.Inject;

/**
 * @author I.A
 * @author Joris Stander
 */

public final class MailerService {
    /**
     * Creates a new {@link MailerService}.
     */
    @Inject MailerClient mailerClient;

    /**
     * Attempts to send an email using the given {@link MailerForm).
     */
    public void sendEmail(MailerForm form) {
        Email email = new Email()
                .setSubject(form.name)
                .setFrom(form.email +" <restartcontactus@gmail.com>")
                .addTo("restartcontactus@gmail.com <restartcontactus@gmail.com>")
                // sends text, HTML or both...
                .setBodyText(form.content + "\n" + "\n" + form.email + "\n" +form.phone);
        mailerClient.send(email);
    }

    public void sendEmail(String title, String to, String message) {
        Email email = new Email()
                .setSubject(title)
                .setFrom("noreply <restartcontactus@gmail.com>")
                .addTo(to + " <" + to + ">")
                // sends text, HTML or both...
                .setBodyText(message);
        mailerClient.send(email);
    }
}
