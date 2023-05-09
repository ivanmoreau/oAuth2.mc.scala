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
        devShells.default = pkgs.mkShell {
            buildInputs = with pkgs; [
              mill
            ];
          };
        packages.default = self'.packages.oauth2;
      };
    };
}
