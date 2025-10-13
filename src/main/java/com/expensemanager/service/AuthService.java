package com.expensemanager.service;

import java.time.Instant;

import com.expensemanager.model.PasswordResetToken;
import com.expensemanager.model.User;
import com.expensemanager.repository.PasswordResetTokenRepository;
import com.expensemanager.repository.UserRepository;
import com.expensemanager.util.PasswordUtil;

public class AuthService {
  private final UserRepository repo = new UserRepository();
  private final PasswordResetTokenRepository tokenRepo = new PasswordResetTokenRepository();
  private final EmailService emailService = new EmailService();

  // Khởi tạo tài khoản admin@example.com nếu chưa tồn tại
  static {
    UserRepository repo = new UserRepository();
    if (repo.findByEmail("admin@example.com") == null) {
      User admin = new User();
      admin.setFullName("Admin");
      admin.setEmail("admin@example.com");
      admin.setUsername("admin");
      admin.setPasswordHash(PasswordUtil.hash("123"));
      admin.setRole("ADMIN");
      admin.setProvider("LOCAL");
      repo.save(admin);
    }
  }

  public User register(String fullName, String email, String password) {
    if (repo.findByEmail(email) != null)
      throw new IllegalArgumentException("Email đã tồn tại");

    User u = new User();
    u.setFullName(fullName);
    u.setEmail(email.toLowerCase());
    // Generate a unique username based on email prefix
    String base = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
    if (base.isBlank()) base = "user";
    String candidate = base;
    int i = 1;
    while (repo.existsByUsername(candidate)) {
      candidate = base + i++;
    }
    u.setUsername(candidate);
    u.setPasswordHash(PasswordUtil.hash(password));
    u.setProvider("LOCAL");
    repo.save(u);
    // Save to Supabase
    new SupabaseUserService().saveUser(u.getUsername(), u.getEmail(), u.getFullName(), u.getProvider());
    return u;
  }

  public User login(String email, String password) {
  User u = repo.findByEmail(email);
  if (u != null && PasswordUtil.verify(password, u.getPasswordHash())) {
    // Nếu là tài khoản admin|admin thì set role ADMIN
    if ("admin".equalsIgnoreCase(u.getUsername()) && "admin".equals(password)) {
      u.setRole("ADMIN");
    }
    return u;
  }
  return null;
  }

  public User loginOrCreateGoogle(String email, String name) {
    User u = repo.findByEmail(email);
    if (u != null) return u;

    User newU = new User();
    newU.setEmail(email.toLowerCase());
    newU.setFullName(name);
    newU.setProvider("GOOGLE");
    // Generate username if missing
    String base = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
    if (base.isBlank()) base = "user";
    String candidate = base;
    int i = 1;
    while (repo.existsByUsername(candidate)) {
      candidate = base + i++;
    }
    newU.setUsername(candidate);
    newU.setPasswordHash(PasswordUtil.hash("google_auth_placeholder"));
    repo.save(newU);
    return newU;
  }

  public boolean sendOtpForReset(String email) {
    User u = repo.findByEmail(email);
    if (u == null) return false;
    String otp = OTPService.generateSixDigits();
    PasswordResetToken t = new PasswordResetToken();
    t.setEmail(email.toLowerCase());
    t.setOtp(otp);
    t.setExpiresAt(Instant.now().plusSeconds(600));
    tokenRepo.save(t);
    String body = "<p>Your OTP code is <b>"+otp+"</b>. It expires in 10 minutes.</p>";
    return emailService.send(email, "Password Reset OTP", body);
  }

  public boolean resetPasswordWithOtp(String email, String otp, String newPassword) {
    PasswordResetToken t = tokenRepo.findValidByEmailAndOtp(email, otp);
    if (t == null) return false;
    User u = repo.findByEmail(email);
    if (u == null) return false;
    u.setPasswordHash(PasswordUtil.hash(newPassword));
    repo.save(u);
    tokenRepo.delete(t);
    return true;
  }
}
