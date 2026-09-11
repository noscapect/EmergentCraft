/** FIFO global inference queue. It limits model work without blocking HTTP or Minecraft ticks. */
export class AsyncQueue {
  private active = 0; private readonly pending: (() => void)[] = [];
  constructor(private readonly maximum = 1) { if (!Number.isInteger(maximum) || maximum < 1) throw new Error("maximum must be at least one"); }
  get queued() { return this.pending.length; } get running() { return this.active; }
  async run<T>(work: () => Promise<T>): Promise<T> { await new Promise<void>(resolve => { const start = () => { this.active++; resolve(); }; if (this.active < this.maximum) start(); else this.pending.push(start); }); try { return await work(); } finally { this.active--; this.pending.shift()?.(); } }
}
