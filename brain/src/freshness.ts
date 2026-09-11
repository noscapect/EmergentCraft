/** A result belongs to exactly the snapshot epoch that initiated it. */
export function isFreshDecision(expectedEpoch: number, receivedEpoch: number): boolean {
  return Number.isSafeInteger(expectedEpoch) && expectedEpoch === receivedEpoch;
}
