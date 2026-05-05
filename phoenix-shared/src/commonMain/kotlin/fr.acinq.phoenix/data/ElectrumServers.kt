package fr.acinq.phoenix.data

import fr.acinq.lightning.io.TcpSocket
import fr.acinq.lightning.utils.ServerAddress


private fun electrumServer(host: String, port: Int = 50002): ServerAddress =
    ServerAddress(host = host, port = port, tls = TcpSocket.TLS.TRUSTED_CERTIFICATES())

private fun electrumServer(host: String, port: Int = 50002, publicKey: String): ServerAddress =
    ServerAddress(host = host, port = port, tls = TcpSocket.TLS.PINNED_PUBLIC_KEY(publicKey))

private fun electrumServerOnion(host: String, port: Int = 50002): ServerAddress =
    ServerAddress(host = host, port = port, tls = TcpSocket.TLS.DISABLED)

val mainnetElectrumServers = listOf(
    electrumServerOnion(host = "electrs.ever-chain.xyz", port = 50001)
)

val testnetElectrumServers = listOf(
    electrumServer(
        host = "testnet.qtornado.com", port = 51002, publicKey =
                "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAwkLgqNkkTbwpV3gMdgDA" +
                "+jJFdzrOp8vIDT/qxVIox8NZ53pxPc2N44aeY1NJx4TyfpHUGcI7l+gxZfLr8a13" +
                "o3CIQSotDbZZdJhS6Ir5tT4iRqwZJch+HayTQf9rztv8OQWgrflWDzCiYtBA5PGx" +
                "6LEQWyah/xPPUbeANe/ndEzlfAhXjNcynSfrkikzTgFNBqnc5CcTkHjYgzCXqMwy" +
                "ZCD6kQTQG+eqIHSHul21dwUougfCWCR+P0zFA7LeUfPz2mLZktmGXjqTyYZ+0ZTU" +
                "gJz/MMZt9PDWGJZsHQzoFSCMicukKtnvZ4Q0gbPOoYp8+WjD4SH+WmC3MZdLagsi" +
                "05hUDdm7PHIM1VHQTALLGRnW3yTOaqhsvYGAM5UOkDcmgUqIr6IztHGWCKldfbhS" +
                "c4l7BIgvwW2M6FxYlSAcavIodNfvEC1ythdMzl8bZsBjGIOZ39WtiM0grgcg7bb8" +
                "W5ovZpLOXpzZBjS0zB0sZJnumjS+3jCSjy9rZXGUn3JmMdqtTV8RQxkB8OBJhFf5" +
                "qtMSZXiJIr9RH71VoJKjnds/hoILHuCKU3HOJeo0+4KSD8+q4g3tZLr/haIrsHg5" +
                "uifT9db6tDML1PTKpbHkW+f3w9PdhSmsNUUXrgNmQ0MoBhxV7U2Qcug3jX3xaf1P" +
                "gwWDg3nZZizhuvBceY0IYLECAwEAAQ=="
    ),
    electrumServer(host = "blockstream.info", port = 993),
    electrumServer(
        host = "testnet.aranguren.org", port = 51002, publicKey =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA+RL/AH7wn08YKlRCswER" +
                "M0JBdGycRuGBgQbM0guzDxi7Ov01WB9AM0DX7GC09pAOefbRP8QzXEhyKO0qpnin" +
                "+5Vz4jKS+xv4zPGx2MpTqjJxzom/6v13cumZxWXMzVSeNUTjVp2sOPQ1JQaHqqjs" +
                "2lTgShWu+pAgKUH1KPWxMSz21cI+AQkT8NuuXe0USYYIeiXzyTpciIaBf50j6185" +
                "u+4bUwA3hvdPZyrkJDtSluJ0HiJzCSFlmNYNHLqbvZNAYrgUM3qJRTsvmD0JK6mm" +
                "8m7iXW4m6mKX22VgR93meD/3rdcrJ8FbMbVlkS3wimzcYezls9JytaXupyeRKhQj" +
                "TQIDAQAB"
    ),
)

val mainnetElectrumServersOnion: List<ServerAddress> by lazy {
    listOf(
        electrumServerOnion(host = "22mgr2fndslabzvx4sj7ialugn2jv3cfqjb3dnj67a6vnrkp7g4l37ad.onion", port = 50001),
        electrumServerOnion(host = "bejqtnc64qttdempkczylydg7l3ordwugbdar7yqbndck53ukx7wnwad.onion", port = 50001),
        electrumServerOnion(host = "egyh5mutxwcvwhlvjubf6wytwoq5xxvfb2522ocx77puc6ihmffrh6id.onion", port = 50001),
        electrumServerOnion(host = "explorerzydxu5ecjrkwceayqybizmpjjznk5izmitf2modhcusuqlid.onion", port = 110),
        electrumServerOnion(host = "kittycp2gatrqhlwpmbczk5rblw62enrpo2rzwtkfrrr27hq435d4vid.onion", port = 50001),
        electrumServerOnion(host = "qly7g5n5t3f3h23xvbp44vs6vpmayurno4basuu5rcvrupli7y2jmgid.onion", port = 50001),
        electrumServerOnion(host = "rzspa374ob3hlyjptkdgz6a62wim2mpanuw6m3shlwn2cxg2smy3p7yd.onion", port = 50003),
        electrumServerOnion(host = "ty6cgwaf2pbc244gijtmpfvte3wwfp32wgz57eltjkgtsel2q7jufjyd.onion", port = 50001),
        electrumServerOnion(host = "udfpzbte2hommnvag5f3qlouqkhvp3xybhlus2yvfeqdwlhjroe4bbyd.onion", port = 60001),
        electrumServerOnion(host = "v7gtzf7nua6hdmb2wtqaqioqmesdb4xrlly4zwr7bvayxv2bpg665pqd.onion", port = 50001),
        electrumServerOnion(host = "v7o2hkemnt677k3jxcbosmjjxw3p5khjyu7jwv7orfy6rwtkizbshwqd.onion", port = 57001),
        electrumServerOnion(host = "venmrle3xuwkgkd42wg7f735l6cghst3sdfa3w3ryib2rochfhld6lid.onion", port = 50001),
        electrumServerOnion(host = "wsw6tua3xl24gsmi264zaep6seppjyrkyucpsmuxnjzyt3f3j6swshad.onion", port = 50001),
    )
}

val testnetElectrumServersOnion by lazy {
    listOf(
        electrumServerOnion(host = "explorerzydxu5ecjrkwceayqybizmpjjznk5izmitf2modhcusuqlid.onion", port = 143)
    )
}

expect fun platformElectrumRegtestConf(): ServerAddress
