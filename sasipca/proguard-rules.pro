# Ignorar avisos de classes em falta de criptografia do Nimbus/Tink que o MSAL traz por herança
-dontwarn com.google.crypto.tink.subtle.**

# Ignorar classes de ecrãs duplos da Microsoft (Surface Duo) que excluímos no gradle
-dontwarn com.microsoft.device.display.**

# Ignorar anotações de debug do FindBugs usadas pelas bibliotecas internas da Microsoft
-dontwarn edu.umd.cs.findbugs.annotations.**