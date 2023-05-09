{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    flake-parts.url = "github:hercules-ci/flake-parts";
  };

  outputs = inputs@{ self, nixpkgs, flake-parts, ... }:
    flake-parts.lib.mkFlake { inherit inputs; } {
      systems = nixpkgs.lib.systems.flakeExposed;
      perSystem = { self', pkgs, lib, config, system, ... }: {
        packages.oauth2 = pkgs.stdenv.mkDerivation {
          name = "oauth2";
          src = self;
          buildInputs = with pkgs; [
            mill
          ];
          buildPhase = ''
            export HOME=$(mktemp -d)
            mill --home $HOME -D coursier.home=$HOME/coursier -D ivy.home=$HOME/.ivy2  -D user.home=$HOME plugin.assembly
          '';
          installPhase = ''
            mkdir -p $out/jar
            cp out/plugin/assembly.dest/out.jar $out/jar/plugin.jar
          '';
        };
        packages.schemagen = pkgs.stdenv.mkDerivation {
          name = "SQL Schema Generator";
          phases = [ "installPhase" ];
          buildInputs = with pkgs; [
            sqlite
          ];
          installPhase = ''
            mkdir -p $out/bin
            echo "${pkgs.sqlite}/bin/sqlite3 authdata.db << EOF
            CREATE TABLE position_player (
              id INTEGER PRIMARY KEY,
              x REAL,
              y REAL,
              z REAL
            );

            CREATE TABLE player_db (
              name TEXT,
              id INTEGER PRIMARY KEY
            );

            CREATE TABLE logged_player (
              id INTEGER PRIMARY KEY,
              logged INTEGER
            );

            CREATE TABLE google_id (
              id INTEGER PRIMARY KEY,
              google_id TEXT
            );
            EOF" > gen-schema
            cp gen-schema $out/bin/gen-schema
            chmod +x $out/bin/gen-schema
          '';
        };  

        devShells.default = pkgs.mkShell {
            buildInputs = with pkgs; [
              mill
            ];
          };
        packages.default = self'.packages.oauth2;
      };
    };
}
