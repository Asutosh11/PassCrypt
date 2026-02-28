package com.lightdarktools.passcrypt.data

object LegalContent {
    const val PRIVACY_POLICY = """
# Privacy Policy
Last Updated: February 2026

PassCrypt is built as a **Privacy-First, Zero-Knowledge** offline application.

## 1. Data Collection & Network
We do not collect, store, or transmit any of your personal data.
- **Zero Network Access**: This app does not request the INTERNET permission. You can verify this in Settings → Apps → PassCrypt → Mobile data & Wi-Fi (it will show 0 B). it is physically impossible for your data to leave your device.
- **No Servers**: We do not maintain any back-end servers or cloud storage.
- **No Analytics**: We do not track usage or collect telemetry.

## 2. Data Storage & Master Key
All credentials are stored exclusively on your device in a secure vault.
- **Local Encryption**: Data is scrambled using industry-standard AES-256 (via SQLCipher).
- **The Master Key**: Your encryption key (Master Key) is tied strictly to your device's secure credentials (Biometrics or PIN/Password). This key never leaves the secure enclave of your hardware.

## 3. Data Transfer & Backups
All data movement is handled **offline** and under your direct control:
- **Direct Transfer**: Moving data directly between Android devices (QR Code) happens over a local connection. No data ever touches the internet or our servers.
- **Local Backups**: When you export to PDF or CSV, the file is generated entirely on your device. You are responsible for the security of these files once they are saved to your storage.

## 4. Hardware Permissions
- **Biometrics/PIN**: Used only for local vault authentication. We never see or store your biometric data or device passcode.
- **Storage**: Used only to maintain the encrypted local database file and to save your manual exports (PDF/CSV).
- **Camera**: Used only to scan vault transfer QR codes.

## 5. Open Source & Transparency
PassCrypt is **Open Source software**. We believe that security software should be auditable by everyone.
- **View the Code**: You can inspect our entire source code, build process, and security implementation on GitHub.
- **GitHub Repository**: [https://github.com/Asutosh11/PassCrypt](https://github.com/Asutosh11/PassCrypt)

## 6. Your Rights
You have absolute control. Deleting the app or clearing app data will permanently erase all credentials as we hold no backups.

---
*This Privacy Policy is for the PassCrypt mobile App, now available for Android.*
    """

    const val TERMS_OF_SERVICE = """
# Terms of Service
Last Updated: February 2026

By using PassCrypt, you agree to the following terms:

## 1. No Data Recovery
PassCrypt is a 100% offline application. We do not hold backups of your data. If you lose your device or forget your device's Master Key (Biometrics or PIN/Password), we cannot recover your data.

## 2. User Responsibility
You are solely responsible for:
- Maintaining the physical and digital security of your device.
- Ensuring your device's secondary credentials (PIN/Password) are secure if biometrics fail.
- **Secure Backups**: If you export your vault to a CSV file (e.g., for iOS migration), you acknowledge that this file is **unencrypted**. You must delete it immediately after use or store it in a secure, encrypted location.

## 3. Security Disclaimer
While we use industry-standard AES-256 encryption, no digital system can be guaranteed 100% secure. You use PassCrypt at your own risk.

## 4. Limitation of Liability
The developers of PassCrypt shall not be liable for any data loss, hardware failure, or security breaches on your device resulting from its use.

## 5. Acceptable Use
You agree not to use the app for any illegal purposes or to store credentials that violate local or international laws.

## 6. Open Source License
PassCrypt is released under the **GNU General Public License v3.0 (GPL-3.0)**. You are free to view, modify, and redistribute the code under the terms of this license.
- **Source Code**: [https://github.com/Asutosh11/PassCrypt](https://github.com/Asutosh11/PassCrypt)

## 7. Changes to Terms
We reserve the right to update these terms at any time.

---

*These Terms of Service are for the PassCrypt mobile App, now available for Android.*

### Attributions
PassCrypt is an **Open Source** project. View the full code at:
[https://github.com/Asutosh11/PassCrypt](https://github.com/Asutosh11/PassCrypt)

This app is made possible by incredible open-source software:
- **SQLCipher** (Zetetic, LLC)
- **PDFBox-Android** (Tom Roush / Apache Software Foundation)
    """
}
