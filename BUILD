load(
    "@com_googlesource_gerrit_bazlets//:gerrit_plugin.bzl",
    "gerrit_plugin",
    "gerrit_plugin_tests",
)

gerrit_plugin(
    name = "admin-console",
    srcs = glob(["src/main/java/**/*.java"]),
    manifest_entries = [
        "Gerrit-PluginName: admin-console",
        "Gerrit-SshModule: com.googlesource.gerrit.plugins.adminconsole.SshModule",
        "Implementation-Title: Plugin admin-console",
        "Implementation-URL: https://gerrit-review.googlesource.com/#/admin/projects/plugins/admin-console",
    ],
    resources = glob(["src/main/resources/**/*"]),
)

gerrit_plugin_tests(
    name = "admin-console_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    tags = ["admin-console"],
    deps = [
        ":admin-console__plugin",
    ],
)
