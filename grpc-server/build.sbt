import sbt.nio.file.FileAttributes

name := "grpc-server"

libraryDependencies ++= Dependencies.grpc

extensionClasses ++= Seq(
  "com.wavesplatform.api.grpc.GRPCServerExtension",
  "com.wavesplatform.events.BlockchainUpdates"
)

inConfig(Compile)(
  Seq(
    Compile / PB.protoSources := Seq(PB.externalIncludePath.value),
    PB.generate / includeFilter := new SimpleFileFilter(
      (f: File) =>
        ((** / "waves" / "node" / "grpc" / ** / "*.proto") || (** / "waves" / "events" / ** / "*.proto"))
          .accept(f.toPath, FileAttributes(f.toPath).getOrElse(FileAttributes.NonExistent))
    ),
    PB.targets += scalapb.gen(flatPackage = true) -> sourceManaged.value
  )
)

enablePlugins(RunApplicationSettings, ExtensionPackaging)
