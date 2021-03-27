class SensorData {
    temperature: number;
    location: string;
    instant: Date;
    id?: string;

    constructor(temperature: number, location: string, instant: Date, id?: string) {
        this.temperature = temperature;
        this.location = location;
        this.instant = instant;
        this.id = id;
    }
}

export default SensorData