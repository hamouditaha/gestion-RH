package com.gestionpresence.service;

import com.gestionpresence.model.BulletinSalaire;
import com.gestionpresence.model.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private TemplateEngine templateEngine;
    
    public void envoyerBulletinSalaire(Employee employee, BulletinSalaire bulletin) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(employee.getEmail());
            helper.setSubject("Votre Bulletin de Salaire - " + 
                bulletin.getPeriodeDebut().format(DateTimeFormatter.ofPattern("MMMM yyyy")));
            
            // Préparer le contexte pour le template Thymeleaf
            Context context = new Context();
            context.setVariable("employee", employee);
            context.setVariable("bulletin", bulletin);
            
            // Générer le HTML du bulletin
            String htmlContent = templateEngine.process("bulletin-salaire", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
            // Marquer le bulletin comme envoyé
            bulletin.setEnvoye(true);
            bulletin.setDateEnvoi(java.time.LocalDate.now());
            
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }
}