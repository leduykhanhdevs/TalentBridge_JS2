# Gmail OTP relay on Render Free

This relay sends password-reset OTP messages through the user's Gmail account using Google Apps Script's HTTPS web-app endpoint. It avoids Render Free's blocked SMTP ports and does not require a `talentbridge.vn` mailbox or paid Render compute.

## 1. Create the relay in Google Apps Script

1. Sign in to [script.google.com](https://script.google.com/) with the Gmail account that should send TalentBridge messages.
2. Create a project and replace its `Code.gs` contents with the sibling `Code.gs` file in this folder.
3. In **Project Settings → Script properties**, add `TALENTBRIDGE_SHARED_SECRET` with a newly generated random value of at least 32 bytes. Keep it private.
4. Select **Deploy → New deployment → Web app**. Set **Execute as** to your account and access to **Anyone** so the Render backend can call it. The relay verifies the shared secret on every POST.
5. Deploy, authorize the script to send email, and copy the web-app URL ending in `/exec`.

## 2. Configure the Render backend

In the `talentbridge-backend` service's Environment page, set:

```text
PASSWORD_RESET_MAIL_ENABLED=true
PASSWORD_RESET_MAIL_PROVIDER=apps-script
GOOGLE_APPS_SCRIPT_URL=<deployed web-app URL ending in /exec>
GOOGLE_APPS_SCRIPT_SECRET=<the same secret stored in Script properties>
```

The `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, and `MAIL_PASSWORD` values are not used by this provider. Do not put the shared secret in frontend variables or commit it to the repository. Save the environment and deploy/restart the backend.

## 3. Verify delivery

Request a password-reset code using an account email registered in TalentBridge. Check the Apps Script **Executions** page and the Render backend logs. A successful relay execution and an email in the inbox (or Spam) are both required to confirm delivery.

Personal Gmail accounts currently have a quota of 100 email recipients per day for Apps Script MailApp. Google controls this quota and can change it. This is suitable for testing and low-volume use, not bulk email.

## Security and operational notes

- The endpoint is public so Render can call it without a Google login; access is restricted by the high-entropy shared secret stored only in Apps Script Script properties and Render Environment.
- The backend only uses it for password-reset OTP messages and rate-limits OTP verification attempts.
- Rotate the secret in both locations if it is exposed. Do not paste it into chat, source code, screenshots, or commit history.
- The app sends the email from the Gmail account that owns the Apps Script project. The receiver can be any email address entered for an existing TalentBridge account.
