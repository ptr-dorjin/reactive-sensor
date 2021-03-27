import {
    BufferEncoders,
    encodeAndAddWellKnownMetadata,
    MAX_STREAM_ID,
    MESSAGE_RSOCKET_COMPOSITE_METADATA,
    MESSAGE_RSOCKET_ROUTING,
    RSocketClient
} from 'rsocket-core';
import RSocketWebSocketClient from 'rsocket-websocket-client';
import {ReactiveSocket} from "rsocket-types";
import {Payload} from "rsocket-types/ReactiveSocketTypes";

const endpoint = "api.v1.sensors.stream";

export default class MessageService {
    client: RSocketClient<any, any>
    connectCallback: Function
    messageCallback: Function

    constructor(connectCallback: Function, messageCallback: Function) {
        this.client = new RSocketClient({
            setup: {
                keepAlive: 5000,
                lifetime: 60000,
                dataMimeType: 'application/json',
                metadataMimeType: MESSAGE_RSOCKET_COMPOSITE_METADATA.string,
            },
            transport: new RSocketWebSocketClient({
                    url: `ws://localhost:7000/rsocket`
                },
                BufferEncoders)
        });
        this.connectCallback = connectCallback;
        this.messageCallback = messageCallback;
    }

    connect() {
        const metadata = encodeAndAddWellKnownMetadata(
            Buffer.alloc(0),
            MESSAGE_RSOCKET_ROUTING,
            Buffer.from(String.fromCharCode(endpoint.length) + endpoint)
        );

        this.client.connect().then(
            (socket: ReactiveSocket<any, any>) => {
                socket.requestStream({metadata})
                    .subscribe({
                        onNext: (payload: Payload<any, any>) => this.messageCallback(null, JSON.parse(payload.data)),
                        onError: (error: any) => this.messageCallback(error),
                        onSubscribe: (subscription: any) => subscription.request(MAX_STREAM_ID),
                    })

                this.connectCallback(null);
            },
            (error: any) => this.connectCallback(error),
        );
    }

    close() {
        if (this.client) {
            this.client.close();
        }
    }
}