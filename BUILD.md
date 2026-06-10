# Building ProjectAddons

ProjectAddons is compiled against **ProjectKorra 1.13.0** and **Spigot 1.21.11**
and targets **Java 21**.

ProjectKorra `1.13.0` is not yet published to Maven Central (Central currently
only has up to `1.12.x`). Until it is released there, you must build it from the
`wip` branch of the ProjectKorra fork once and install it into your local Maven
repository.

## Prerequisites

- JDK 21
- Maven 3.8+

## 1. Install ProjectKorra 1.13.0 into your local Maven repo (one time)

```bash
git clone https://github.com/ElementalMC/ProjectKorra.git
cd ProjectKorra
git checkout wip
mvn install -DskipTests -Dgpg.skip=true -Dmaven.javadoc.skip=true -Dmaven.source.skip=true
```

This installs `com.projectkorra:projectkorra:1.13.0` into `~/.m2`. The
`-Dgpg.skip` / `-Dmaven.javadoc.skip` / `-Dmaven.source.skip` flags skip the
release-signing/publishing steps that would otherwise fail on a normal dev
machine.

> Already have the fork cloned? Just `git checkout wip && git pull` in it and run
> the `mvn install ...` line above.

## 2. Build ProjectAddons

```bash
mvn clean package
```

Output: `target/ProjectAddons-1.4.0-PK1.13.0.jar`

Once ProjectKorra 1.13.0 is published to Maven Central, step 1 will no longer be
necessary.
