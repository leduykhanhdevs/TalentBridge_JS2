function doPost(event) {
  try {
    const expectedSecret = PropertiesService.getScriptProperties()
      .getProperty('TALENTBRIDGE_SHARED_SECRET');
    const payload = JSON.parse(event.postData.contents || '{}');

    if (!expectedSecret || !constantTimeEquals(payload.secret, expectedSecret)) {
      return jsonResponse({ ok: false, error: 'unauthorized' });
    }

    const recipient = String(payload.to || '').trim();
    const textBody = String(payload.textBody || '');
    const htmlBody = String(payload.htmlBody || '');
    if (!isValidEmail(recipient) || !textBody || !htmlBody
        || textBody.length > 4000 || htmlBody.length > 12000) {
      return jsonResponse({ ok: false, error: 'invalid_request' });
    }

    MailApp.sendEmail({
      to: recipient,
      subject: String(payload.subject || 'TalentBridge - Mã OTP đặt lại mật khẩu').slice(0, 160),
      body: textBody,
      htmlBody: htmlBody,
      name: 'TalentBridge'
    });

    return jsonResponse({ ok: true });
  } catch (error) {
    console.error('OTP relay failed: ' + String(error));
    return jsonResponse({ ok: false, error: 'delivery_failed' });
  }
}

function isValidEmail(email) {
  return email.length <= 254 && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function constantTimeEquals(candidate, expected) {
  if (typeof candidate !== 'string' || typeof expected !== 'string') return false;
  let difference = candidate.length ^ expected.length;
  const length = Math.max(candidate.length, expected.length);
  for (let index = 0; index < length; index += 1) {
    difference |= (candidate.charCodeAt(index) || 0) ^ (expected.charCodeAt(index) || 0);
  }
  return difference === 0;
}

function jsonResponse(body) {
  return ContentService.createTextOutput(JSON.stringify(body))
    .setMimeType(ContentService.MimeType.JSON);
}
