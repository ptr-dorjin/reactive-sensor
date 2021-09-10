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

let RSOCKET_HOST = (window as any)._env_?.SENSOR_SERVER_HOST
if (!RSOCKET_HOST) {
    RSOCKET_HOST = window.location.hostname
    console.log("Defaulting to host", RSOCKET_HOST)
}
let RSOCKET_PORT = (window as any)._env_?.SENSOR_SERVER_PORT
if (!RSOCKET_PORT) {
    RSOCKET_PORT = "7000"
    console.log("Defaulting to port", RSOCKET_PORT)
}
const RSOCKET_URL = `ws://${RSOCKET_HOST}:${RSOCKET_PORT}/rsocket`;
console.log("Connecting via RSocket to", RSOCKET_URL)

const endpoint = "api.v2.sensors.stream";

export default class MessageService {
    client: RSocketClient<any, any>
    onConnect: Function
    onNextSensorData: Function

    constructor(onConnect: Function, onNextSensorData: Function) {
        this.client = new RSocketClient({
            setup: {
                keepAlive: 5000,
                lifetime: 60000,
                dataMimeType: 'application/json',
                metadataMimeType: MESSAGE_RSOCKET_COMPOSITE_METADATA.string,
            },
            transport: new RSocketWebSocketClient({
                    url: RSOCKET_URL
                },
                BufferEncoders)
        });
        this.onConnect = onConnect;
        this.onNextSensorData = onNextSensorData;
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
                        onNext: (payload: Payload<any, any>) => this.onNextSensorData(null, JSON.parse(payload.data)),
                        onError: (error: any) => this.onNextSensorData(error),
                        onSubscribe: (subscription: any) => subscription.request(MAX_STREAM_ID),
                    })

                this.onConnect(null);
            },
            (error: any) => this.onConnect(error),
        );
    }

    close() {
        if (this.client) {
            this.client.close();
        }
    }
}