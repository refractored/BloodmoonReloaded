jdk: openjdk21
before_install:
  - source "$HOME/.sdkman/bin/sdkman-init.sh"
  - sdk update
  - sdk install java 22.0.2-tem
  - sdk use java 22.0.2-tem