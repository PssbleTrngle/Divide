export interface IText {
    text: string;
}

export interface IFlag {
    rank: number;
    protection: number;
}


export interface ITeam {
    display: IText;
    name: string,
    rank: number,
    flags: IFlag[],
    color?: number
}

export interface IOverview {
    nextDelivery: number;
    teams: ITeam[];
}