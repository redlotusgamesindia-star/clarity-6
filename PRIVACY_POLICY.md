# Privacy Policy for Clarity

**Last updated:** July 6, 2026

Clarity ("the App") is developed and published by Unbroken Self ("we," "us," "our"). This policy explains what data the App collects, how it is used, and — just as importantly — what never leaves your device. It applies to the Clarity Android app (package name `com.runtimelabs.clarity`) available on Google Play.

Clarity is a private, offline-first recovery companion for people working to reduce or quit compulsive pornography use. We built the App around a simple rule: **your recovery data is yours, stored only on your device, and encrypted at rest.** The one deliberate exception to that — a small banner ad, and what it requires — is explained in full below, not buried.

---

## 1. Data that never leaves your device

The App stores the following locally, in a database encrypted with SQLCipher (AES‑256), using a key generated on-device and held in the Android Keystore:

- Your onboarding profile (age range, goals, motivations, and similar self-reported context)
- Your daily check-ins (mood, urge intensity)
- Your streak and relapse history, and any optional reflection notes you add after a setback
- Journal entries, thought records, and gratitude entries
- Habits you create and their completion history

**None of the above is transmitted to us, to Google, to any analytics service, or to any other third party.** There is no account system and no backend server for this data — we could not access it even if compelled to, because it simply never reaches us. If you uninstall the App, this data is deleted from your device along with it. We do not offer cloud backup or sync for this data, by design.

## 2. Data involved in showing a banner ad

Clarity shows a small banner advertisement on a few non-sensitive screens (Home, Journey, Learn, Journal, and Settings) via **Google Mobile Ads (AdMob)**. Ads are never shown during onboarding, breathing exercises, the guided recovery flow, the relapse support flow, the crisis toolkit, journaling or reflection screens, loading screens, or dialogs, and are never shown to a Premium user.

To do this honestly and legally, the App also uses **Google's User Messaging Platform (UMP)** to ask for your consent (where required by your region, such as the EEA, UK, and Switzerland) before any ad-related data processing occurs.

Serving an ad and gathering that consent involves the following, handled by Google, not by us directly:

- Your advertising identifier and general device information
- Approximate location derived from your IP address (not precise GPS location — the App requests no location permission)
- Ad interaction data (impressions, clicks)
- Your consent choices, so they can be remembered and honored

We do not receive, see, or store this information ourselves — it is processed by Google's advertising systems under Google's own privacy terms. You can review your ad choices and consent at any time from **Settings → Privacy & ad choices** inside the App, and you can review Google's practices directly:

- [Google Mobile Ads privacy & security](https://support.google.com/admob/answer/6128543)
- [How Google uses data when you use our partners' sites or apps](https://policies.google.com/technologies/partner-sites)

**The privacy-relevant trade-off, stated plainly:** restricting ads to non-sensitive screens prevents an ad from appearing during a difficult moment, but it does not prevent Google's advertising systems from learning that a given device runs Clarity at all, since that happens whenever the ad SDK is active — regardless of which specific screen triggered it. If this matters to you, Clarity's ad-free Premium option (see below) removes this entirely.

## 3. Premium / ad removal

Clarity includes a "Remove Ads" option in Settings. As of this version, this is a **Coming Soon** placeholder — no purchases are processed by the App yet. When purchasing is enabled, it will be handled through **Google Play's Billing system**, and this policy will be updated to describe exactly what that involves before it ships.

## 4. Permissions the App requests, and why

| Permission | Purpose |
|---|---|
| `INTERNET` | Required by the Google Mobile Ads SDK to request and display a banner ad, and by UMP to gather consent. Not used for anything else. |
| `ACCESS_NETWORK_STATE` | Required by the Google Mobile Ads SDK to check connectivity before requesting an ad. |
| `com.google.android.gms.permission.AD_ID` | Required by the Google Mobile Ads SDK to access the Android advertising identifier. |
| `POST_NOTIFICATIONS` | Used only for local habit reminders you configure yourself. These reminders are generated and delivered entirely on your device — no reminder content or schedule is sent anywhere. |
| `RECEIVE_BOOT_COMPLETED` | Used only to re-arm your local habit reminders after your device restarts. |

The App requests no location, contacts, camera, microphone, storage, or biometric permissions, because it has no feature that needs them.

## 5. Children's privacy

Clarity is intended for adults and is not directed at children. We do not knowingly collect personal information from anyone under 18. If you believe a child has provided us information, contact us using the details below and we will address it.

## 6. Your choices and rights

Because your recovery data lives only on your device:

- **Access and deletion**: uninstalling the App permanently deletes all locally stored data. There is no separate account or server copy to request deletion from.
- **Ad personalization**: manage your consent and ad-personalization choices anytime via **Settings → Privacy & ad choices**, or through your Google Account's [Ad Settings](https://adssettings.google.com/).
- **Notifications**: habit reminders can be disabled per-habit inside the App, or all app notifications can be disabled via your device's system settings.

If you are located in the EEA, UK, Switzerland, California, or another jurisdiction with its own data protection law (such as the GDPR or CCPA/CPRA), you may have additional rights concerning the limited data described in Section 2. Because that data is processed by Google as described above, you can also exercise relevant rights directly with Google via the links in Section 2.

## 7. Data security

Recovery data is encrypted at rest using SQLCipher with a key generated and stored in the Android Keystore, independent of any app-level PIN or lock you may set. Automatic cloud backup of app data is explicitly disabled at the system level, so this data cannot leave your device via Android's backup mechanisms either.

No method of storage is perfectly infallible, but no part of this data is ever transmitted off-device in the first place, which removes an entire category of risk by design rather than by policy.

## 8. Changes to this policy

If how the App handles data changes — for example, when Premium purchasing is implemented — we will update this policy and change the "Last updated" date above. Material changes will be reflected in the App's release notes.

## 9. Contact us

Questions about this policy or how Clarity handles data:

**Unbroken Self**
📧 unbrokenselfsupport@gmail.com
🔗 _[replace with your GitHub repository or project URL]_

---

*This policy describes Clarity's actual data handling as implemented in the app's source code as of the date above. It is provided as an accurate technical account, not legal advice — if you are publishing this app commercially, especially given its subject matter, we'd recommend having a lawyer familiar with app privacy law (GDPR, CCPA/CPRA, and Google Play's Developer Program Policies) review this before publishing.*
