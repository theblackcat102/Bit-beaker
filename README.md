# Bitbeaker - A Bitbucket client for Android

## LICENSE

Licensed under the Apache License, Version 2.0. For complete licensing information
and list of third party libraries used in this project, see [LICENSE.md](LICENSE.md).


## BUGS AND FEATURES

If you find a bug or need a feature, please report it to <https://bitbucket.org/bitbeaker-dev-team/bitbeaker/issues>.
Please keep in mind that this is a free software with very few volunteers working on it in
their free time. You should also check if the fix or feature is already mentioned in *Next release*
section of [Changelog](https://bitbucket.org/bitbeaker-dev-team/bitbeaker/wiki/Changelog). If it is, you can
try the [debug build](https://drone.io/bitbucket.org/bitbeaker-dev-team/bitbeaker/files). Use vote the help
prioritising the issues.


## CONTRIBUTION

Contribution is very welcome! If you want to add a feature or fix a bug yourself, please fork the
repository at <https://bitbucket.org/bitbeaker-dev-team/bitbeaker>, do your changes and send a pull request.
However, it may be a good idea to discuss your intentions in the issue tracker before you start coding.
See also [Contributing guidelines](https://bitbucket.org/bitbeaker-dev-team/bitbeaker/wiki/Contributing).


### Getting started

First make sure you have [set a username in Mercurial](https://www.mercurial-scm.org/wiki/QuickStart#Setting_a_username).
Add something like this to `~/.hgrc` (*nix) or `%USERPROFILE%\Mercurial.ini` (Windows):

	[ui]
	username = John Doe <john@example.com>

For your own developing, register yourself a new OAuth consumer in Bitbucket (`Manage account -> Integrated applications -> Add consumer`).

The rest of this guide assumes you use Android Studio:  
(Tested with Linux Mint 17.2, Android Studio 1.5.1)

1. `Check out project from Version Control` -> `Mercurial` -> `Clone`. Once cloning is finished, a prompt will open asking if you want to open build.gradle. Just leave the prompt open for now.
2. Copy `app/gradle.properties.template` to `app/gradle.properties` and fill in your OAuth key and secret.
3. Using SDK Manager, install
    1. `Android Support Repository`
    2. Check from `app/build.gradle` which versions are required from these:
         1. `Android SDK Tools`
         2. `Android SDK Platform-tools`
         3. `Android SDK Build-tools`
         4. `SDK Platform` (Android API)
4. Import project into Android Studio: continue with the prompt from step 1 (or `Import Project` -> select `build.gradle`).

More tips using Android Studio can be found in our [Wiki](https://bitbucket.org/bitbeaker-dev-team/bitbeaker/wiki/Android_Studio).


### Translations

Translations are very welcome! Please use <https://crowdin.com/project/bitbeaker>.


## CONTACT

Use the issue tracker for bug reports and feature requests. You can also come to say hi to our
public HipChat channel at <http://www.hipchat.com/gUetcIqT8>.


## IMPORTANT LINKS

- [Debug builds and test reports at Drone.io](https://drone.io/bitbucket.org/bitbeaker-dev-team/bitbeaker/files)
- [Bitbeaker at Google Play](https://play.google.com/store/apps/details?id=fi.iki.kuitsi.bitbeaker)
- [Code statistics of Bitbeaker at Black Duck Open Hub](https://www.openhub.net/p/bitbeaker)
- [Bitbucket REST API documentation](https://developer.atlassian.com/bitbucket/api/2/reference/)


## Project Status

[![Build Status: bitbeaker-dev-team/bitbeaker](https://drone.io/bitbucket.org/bitbeaker-dev-team/bitbeaker/status.png)](https://drone.io/bitbucket.org/bitbeaker-dev-team/bitbeaker/latest)
[![Crowdin](https://d322cqt584bo4o.cloudfront.net/bitbeaker/localized.png)](https://crowdin.com/project/bitbeaker)
