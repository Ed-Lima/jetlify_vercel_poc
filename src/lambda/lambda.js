// kept in lambda memory during hot load
console.log('cold start');

exports.handler = async (event, context) => {
  console.log(`test started`);
};
