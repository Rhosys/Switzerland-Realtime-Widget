# Switzerland Realtime Widget — Todo

## Infrastructure Todos (require out-of-repo work)

These cannot be completed from within this repository. They require access to
`_rhosys-apps-infra`, the `_tools` repo, AWS, GCP Console, and Google Play Console.

### 🔲 1. Generate the upload keystore
In the `_tools` repo:
```bash
generate-android-keystore --alias realtime > deployment/android-upload-signing.json
```
(RSA-4096, PKCS12, 30-year validity, password encrypted via AWS KMS
`alias/deployment-encryption-key` in `eu-west-1`.)
Commit the resulting JSON to this repo, replacing the copied Ruby Slippers key, and
update the key alias in `.gitlab-ci.yml` and `docs/SIGNING.md` to `realtime`.

### 🔲 2. GCP infra — add WIF binding for this project (`_rhosys-apps-infra/gcp/main.tf`)
Add a `google_service_account_iam_member` resource to bind the WIF
`principalSet` for this GitLab project path to the Play Store service account:

```hcl
resource "google_service_account_iam_member" "switzerland_realtime_widget_gitlab_wif" {
  service_account_id = google_service_account.gitlab_play_store.name
  role               = "roles/iam.workloadIdentityUser"
  member             = "principalSet://iam.googleapis.com/projects/454629444494/locations/global/workloadIdentityPools/gitlab-oidc/providers/gitlab-com/attribute.project_path/rhosys/rapid/switzerland-realtime-widget"
}
```
**Note:** CEL conditions and wildcards do NOT work for SA impersonation — this
explicit per-app binding is required.

### 🔲 3. Play Console — create the app
1. Create app with package name `ch.rhosys.realtime` in Google Play Console.
2. Upload the first signed AAB manually (required before the API can be used).
3. Grant `gitlab-play-store@rhosys-apps.iam.gserviceaccount.com` the
   **Release Manager** role on this app (Play Console → Setup → API access).

### 🔲 4. GitLab mirror — create the GitLab project
Create a GitLab project mirroring this GitHub repo so the signed-release
deploy pipeline runs there. Set the project variable:
- `AWS_ACCOUNT_ID` = `<ACCOUNT_ID>`

(AWS infra reuses existing `GitLabRunnerRole` + `alias/deployment-encryption-key` —
no AWS changes needed beyond the keystore in Todo 1.)

### 🔲 5. (Optional) Branded launcher icon
Replace the placeholder launcher icon in
`app/src/main/res/drawable/ic_launcher_*.xml` and `mipmap-anydpi-v26/`.

---

## Feature Todos

### 🔲 Re-enable R8 minification
`isMinifyEnabled` is currently commented out in `app/build.gradle.kts`. Re-enable it
before release and verify the proguard rules in `app/proguard-rules.pro` cover all
reflection-heavy dependencies: Hilt, Retrofit + kotlinx-serialization, PostHog, and
the Glance widget. Run `npm run start:release` on the emulator to catch any stripping
crashes before shipping.
