/** FIFO global inference queue. It limits model work without blocking HTTP or Minecraft ticks. */
export class AsyncQueue {
  private active = 0; private readonly foreground: (() => void)[] = []; private readonly background: (() => void)[] = [];
  constructor(private readonly maximum = 1) { if (!Number.isInteger(maximum) || maximum < 1) throw new Error("maximum must be at least one"); }
  get queued() { return this.foreground.length+this.background.length; } get running() { return this.active; }
  async run<T>(work: () => Promise<T>,priority:"foreground"|"background"="foreground"): Promise<T> { await new Promise<void>(resolve => { const start = () => { this.active++; resolve(); }; if (this.active < this.maximum) start(); else (priority==="foreground"?this.foreground:this.background).push(start); }); try { return await work(); } finally { this.active--; (this.foreground.shift()??this.background.shift())?.(); } }
}
