package com.grcontrol.grcontrol_backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Servicio para envío de emails
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${mail.from.name}")
    private String fromName;

    @Value("${mail.from.email}")
    private String fromEmail;

    /**
     * Enviar código de recuperación de contraseña
     */
    public void sendPasswordResetCode(String toEmail, String userName, String resetCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("GRControl - Código de Recuperación de Contraseña");

            String htmlContent = buildPasswordResetEmail(userName, resetCode);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email de recuperación enviado a: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Error al enviar email de recuperación a {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Error al enviar email", e);
        } catch (Exception e) {
            log.error("Error inesperado al enviar email: {}", e.getMessage());
            throw new RuntimeException("Error al enviar email", e);
        }
    }

    /**
     * Construir HTML del email de recuperación
     */
    private String buildPasswordResetEmail(String userName, String resetCode) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white;
                        padding: 30px;
                        text-align: center;
                        border-radius: 10px 10px 0 0;
                    }
                    .content {
                        background: #f9f9f9;
                        padding: 30px;
                        border-radius: 0 0 10px 10px;
                    }
                    .code-box {
                        background: white;
                        border: 2px dashed #667eea;
                        padding: 20px;
                        text-align: center;
                        margin: 20px 0;
                        border-radius: 8px;
                    }
                    .code {
                        font-size: 32px;
                        font-weight: bold;
                        color: #667eea;
                        letter-spacing: 5px;
                        font-family: 'Courier New', monospace;
                    }
                    .warning {
                        background: #fff3cd;
                        border-left: 4px solid #ffc107;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 4px;
                    }
                    .footer {
                        text-align: center;
                        margin-top: 30px;
                        color: #666;
                        font-size: 12px;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>GRControl - Recuperacion de Contrasena</h1>
                    <p>Sistema de Gestion de Grifos</p>
                </div>
                <div class="content">
                    <h2>Hola, %s</h2>
                    <p>Has solicitado recuperar tu contrasena. Utiliza el siguiente codigo de 6 digitos para continuar:</p>
                    
                    <div class="code-box">
                        <div class="code">%s</div>
                    </div>
                    
                    <p>Este codigo es valido por <strong>15 minutos</strong>.</p>
                    
                    <div class="warning">
                        <strong>Importante:</strong><br>
                        Si no solicitaste este cambio, ignora este mensaje y tu contrasena permanecera sin cambios.
                    </div>
                    
                    <p>Saludos,<br><strong>Equipo GRControl</strong></p>
                </div>
                <div class="footer">
                    <p>Este es un mensaje automatico, por favor no responder.</p>
                    <p>2025 GRControl - Todos los derechos reservados</p>
                </div>
            </body>
            </html>
            """, userName, resetCode);
    }

    /**
     * Enviar notificación de cambio de contraseña
     */
    public void sendPasswordChangedNotification(String toEmail, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("GRControl - Contraseña Actualizada");

            String htmlContent = buildPasswordChangedEmail(userName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email de confirmación de cambio de contraseña enviado a: {}", toEmail);
        } catch (Exception e) {
            log.error("Error al enviar email de confirmación: {}", e.getMessage());
            // No lanzar excepción aquí, es solo notificación
        }
    }

    /**
     * Construir HTML del email de confirmación
     */
    private String buildPasswordChangedEmail(String userName) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .header {
                        background: linear-gradient(135deg, #11998e 0%%, #38ef7d 100%%);
                        color: white;
                        padding: 30px;
                        text-align: center;
                        border-radius: 10px 10px 0 0;
                    }
                    .content {
                        background: #f9f9f9;
                        padding: 30px;
                        border-radius: 0 0 10px 10px;
                    }
                    .success-box {
                        background: #d4edda;
                        border-left: 4px solid #28a745;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 4px;
                    }
                    .footer {
                        text-align: center;
                        margin-top: 30px;
                        color: #666;
                        font-size: 12px;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>Contrasena Actualizada</h1>
                </div>
                <div class="content">
                    <h2>Hola, %s</h2>
                    <p>Tu contrasena ha sido actualizada exitosamente.</p>
                    
                    <div class="success-box">
                        <strong>Cambio confirmado</strong><br>
                        Ahora puedes iniciar sesion con tu nueva contrasena.
                    </div>
                    
                    <p>Si no realizaste este cambio, contacta inmediatamente con el administrador del sistema.</p>
                    
                    <p>Saludos,<br><strong>Equipo GRControl</strong></p>
                </div>
                <div class="footer">
                    <p>Este es un mensaje automatico, por favor no responder.</p>
                    <p>2025 GRControl - Todos los derechos reservados</p>
                </div>
            </body>
            </html>
            """, userName);
    }
}
